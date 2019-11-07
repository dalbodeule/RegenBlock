package space.mori.regenblock.Listener;

import org.bukkit.block.data.BlockData;
import org.bukkit.event.entity.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.block.*;
import space.mori.regenblock.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import space.mori.regenblock.Event.*;
import org.bukkit.*;
import java.util.*;
import java.util.regex.*;

public class RegenBlockEventListenerBlock implements Listener
{
    private RegenBlock plugin;
    private Random rnd;
    private static int maxBlocksPerRespawn;
    private static long tryAgainIn;
    private static long timeBetweenRespawn;
    private long lastRespawn;
    private long respawnCounter;
    
    static {
        RegenBlockEventListenerBlock.maxBlocksPerRespawn = 500;
        RegenBlockEventListenerBlock.tryAgainIn = 40L;
        RegenBlockEventListenerBlock.timeBetweenRespawn = 2000L;
    }
    
    public RegenBlockEventListenerBlock(final RegenBlock plugin) {
        this.rnd = new Random();
        this.lastRespawn = System.currentTimeMillis();
        this.respawnCounter = 0L;
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(final BlockBreakEvent event) {
        if (event.isCancelled() || event.getBlock().getType() == Material.AIR) {
            return;
        }
        this.regenBlock(event.getBlock().getLocation(), event.getBlock().getType(), event.getBlock().getBlockData(), event.getPlayer(), true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(final BlockPlaceEvent event) {
        if (event.isCancelled() || event.getBlockPlaced().getType() == Material.AIR) {
            return;
        }
        if (this.getBlockRegion(event.getBlock().getLocation()) != null) {
            event.getBlock().setType(event.getBlock().getType(), false);
        }
        this.regenBlock(event.getBlock().getLocation(), Material.AIR, Bukkit.createBlockData(Material.AIR), event.getPlayer(), false);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(final EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        for (final Block b : event.blockList()) {
            this.regenBlock(b.getLocation(), b.getType(), b.getBlockData(), null, true);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(final BlockBurnEvent event) {
        if (event.isCancelled() || event.getBlock().getType() == Material.FIRE) {
            return;
        }
        final Block b = event.getBlock();
        this.regenBlock(b.getLocation(), b.getType(), b.getBlockData(), null, true);
    }
    
    @EventHandler
    public void onEvent(final RegenBlockEventAlarm event) {
        this.plugin.getQueue().broadcastMessage(event.getAlarmRadius(), event.getAlarmMessage(), event.getWorldName(), event.getX(), event.getY(), event.getZ());
    }
    
    @EventHandler
    public void onEvent(final RegenBlockEventRespawn event) {
        final ArrayList<RegenBlockBlock> blocks = event.getBlocks();
        if (this.respawnCounter >= RegenBlockEventListenerBlock.maxBlocksPerRespawn && System.currentTimeMillis() - this.lastRespawn < RegenBlockEventListenerBlock.timeBetweenRespawn) {
            this.respawnCounter = 0L;
            final ArrayList<RegenBlockBlock> b = blocks;
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new Runnable() {
                @Override
                public void run() {
                    Bukkit.getServer().getPluginManager().callEvent((Event)new RegenBlockEventRespawn(b));
                }
            }, RegenBlockEventListenerBlock.tryAgainIn);
            return;
        }
        for (int i = 0; i < RegenBlockEventListenerBlock.maxBlocksPerRespawn && i < blocks.size(); ++i) {
            final RegenBlockBlock block = blocks.get(i);
            final Block b2 = block.getLocation().getWorld().getBlockAt(block.getLocation());
            b2.setType(block.getType(), false);
            b2.setBlockData(block.getData(), false);
            this.plugin.getConfiguration().removeBlock(b2);
            ++this.respawnCounter;
        }
        this.lastRespawn = System.currentTimeMillis();
        if (blocks.size() > RegenBlockEventListenerBlock.maxBlocksPerRespawn) {
            blocks.subList(0, RegenBlockEventListenerBlock.maxBlocksPerRespawn - 1).clear();
            final ArrayList<RegenBlockBlock> b = blocks;
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)new Runnable() {
                @Override
                public void run() {
                    Bukkit.getServer().getPluginManager().callEvent((Event)new RegenBlockEventRespawn(b));
                }
            }, RegenBlockEventListenerBlock.tryAgainIn);
        }
    }
    
    @EventHandler
    public void onEvent(final RegenBlockEventConfigSave event) {
        if (this.plugin.getConfiguration().isDirty()) {
            this.plugin.getConfiguration().saveBlocks();
        }
    }
    
    public boolean isPlayerEditor(final String playerName) {
        return this.plugin.getListenerPlayer().getPlayerEditStatus().contains(playerName);
    }
    
    public void regenBlock(final Location location, Material material, BlockData data, final Player player, final Boolean isBreakEvent) {
        if (player != null && this.isPlayerEditor(player.getName())) {
            return;
        }
        if (isBreakEvent) {
            if (!this.plugin.getConfiguration().doMonitorBreak()) {
                return;
            }
        }
        else if (!this.plugin.getConfiguration().doMonitorPlace()) {
            return;
        }
        if (this.plugin.getConfiguration().listBlacklistBlockId() != null && this.plugin.getConfiguration().listBlacklistBlockId().contains(String.valueOf(material.getId()))) {
            return;
        }
        if (material == Material.FIRE) {
            material = Material.AIR;
        }
        final String regionName = this.getBlockRegion(location);
        if (this.ignoreBlock(location, material, regionName)) {
            return;
        }
        long respawnTime = this.plugin.getConfiguration().getRegionRespawnTime(regionName);
        respawnTime = System.currentTimeMillis() + respawnTime * 1000L;
        final int syncType = this.plugin.getConfiguration().getRegionSync(regionName);
        final int regionType = this.plugin.getConfiguration().getRegionType(regionName);
        if (regionType == 1 && material != Material.AIR) {
            final HashMap<Material, Integer> spawnBlocksId = this.plugin.getConfiguration().getRegionSpawnBlocks(regionName);
            if (spawnBlocksId.containsKey(material)) {
                Iterator<Material> i = spawnBlocksId.keySet().iterator();
                int totalChance = 0;
                while (i.hasNext()) {
                    totalChance += spawnBlocksId.get(i.next());
                }
                if (totalChance == 0) {
                    return;
                }
                final int roll = this.rnd.nextInt(totalChance);
                Material type = Material.AIR;
                i = spawnBlocksId.keySet().iterator();
                totalChance = 0;
                while (i.hasNext()) {
                    final Material block = i.next();
                    final int blockIdChance = spawnBlocksId.get(block);
                    totalChance += blockIdChance;
                    if (roll <= totalChance && roll >= totalChance - blockIdChance) {
                        type = block;
                        break;
                    }
                }
                material = type;
                data = Bukkit.createBlockData(type);
            }
        }
        if (player != null && ((this.plugin.getConfiguration().getRegionFeedbackID(regionName) == 1 && material == Material.AIR) || this.plugin.getConfiguration().getRegionFeedbackID(regionName) == 2)) {
            final Pattern pat = Pattern.compile("TIME");
            final Matcher mat = pat.matcher(this.plugin.getConfiguration().getFeedbackString());
            this.plugin.getLog().sendPlayerWarn(player, mat.replaceAll(String.valueOf(this.plugin.getConfiguration().getRegionRespawnTime(regionName))));
        }
        this.queueBlock(location, material, data, regionName, respawnTime, syncType);
    }
    
    public String getBlockRegion(final Location location) {
        if (this.plugin.getConfiguration().listRegions() == null) {
            return null;
        }
        for (final String regionName : this.plugin.getConfiguration().listRegions()) {
            final String worldName = this.plugin.getConfiguration().getRegionWorldName(regionName);
            if (!location.getWorld().getName().equalsIgnoreCase(worldName)) {
                continue;
            }
            final int leftX = this.plugin.getConfiguration().getRegionLeftX(regionName);
            final int leftY = this.plugin.getConfiguration().getRegionLeftY(regionName);
            final int leftZ = this.plugin.getConfiguration().getRegionLeftZ(regionName);
            final int rightX = this.plugin.getConfiguration().getRegionRightX(regionName);
            final int rightY = this.plugin.getConfiguration().getRegionRightY(regionName);
            final int rightZ = this.plugin.getConfiguration().getRegionRightZ(regionName);
            if (Math.abs(leftX - rightX) == Math.abs(leftX - location.getBlockX()) + Math.abs(rightX - location.getBlockX()) && Math.abs(leftY - rightY) == Math.abs(leftY - location.getBlockY()) + Math.abs(rightY - location.getBlockY()) && Math.abs(leftZ - rightZ) == Math.abs(leftZ - location.getBlockZ()) + Math.abs(rightZ - location.getBlockZ())) {
                return regionName;
            }
        }
        return null;
    }
    
    private boolean ignoreBlock(final Location location, final Material type, final String regionName) {
        return regionName == null || (this.plugin.getConfiguration().listRegionBlacklistBlockId(regionName) != null && this.plugin.getConfiguration().listRegionBlacklistBlockId(regionName).contains(String.valueOf(type))) || this.plugin.getConfiguration().getBlockToRegen(this.plugin.getConfiguration().getRegionWorldName(regionName), location) != null;
    }
    
    private void queueBlock(final Location location, final Material material, final BlockData data, final String regionName, long respawnTime, final int syncType) {
        if (syncType == 1) {
            final Long rt = this.plugin.getQueue().getRegionRespawnTime(regionName);
            if (rt != null) {
                respawnTime = rt;
            }
        }
        else if (syncType == 2) {
            this.plugin.getQueue().updateRegionRespawnTime(regionName, respawnTime);
        }
        else if (syncType == 3) {
            final long delta = this.plugin.getQueue().shiftRegionRespawnTime(regionName, respawnTime);
            if (delta != 0L) {
                respawnTime = delta;
            }
        }
        this.plugin.getConfiguration().setBlock(location, material, data, regionName, respawnTime);
        this.plugin.getQueue().addBlock(new RegenBlockBlock(location, material, data, regionName, respawnTime, this.plugin.getConfiguration().getRegionAlarmTime(regionName), this.plugin.getConfiguration().getRegionSync(regionName), this.plugin.getConfiguration().getRegionAlarmRadius(regionName), this.plugin.getConfiguration().getRegionAlarmMessage(regionName), this.plugin.getConfiguration().getRegionType(regionName)));
    }
}
