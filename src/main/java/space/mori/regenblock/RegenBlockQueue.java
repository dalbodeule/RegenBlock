package space.mori.regenblock;

import java.util.*;

import org.bukkit.block.data.BlockData;
import org.bukkit.event.*;
import space.mori.regenblock.Event.*;
import org.bukkit.*;
import org.bukkit.entity.*;

public class RegenBlockQueue implements Runnable
{
    private RegenBlock plugin;
    private long queueDelta;
    private long nextConfigSave;
    private long configSaveDelta;
    private HashMap<String, Long> alarmWentOffAt;
    private Vector<RegenBlockBlock> blockQueue;
    
    public RegenBlockQueue(final RegenBlock plugin) {
        this.queueDelta = 1000L;
        this.nextConfigSave = System.currentTimeMillis();
        this.configSaveDelta = 60000L;
        this.alarmWentOffAt = new HashMap<String, Long>();
        this.blockQueue = new Vector<RegenBlockBlock>();
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(this.queueDelta);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.checkQueue();
        }
    }
    
    public void addBlock(final RegenBlockBlock block) {
        this.blockQueue.add(block);
    }
    
    public void regenRegion(final String regionName) {
        final long time = System.currentTimeMillis();
        final Vector<RegenBlockBlock> cloneBlockQueue = (Vector<RegenBlockBlock>)this.blockQueue.clone();
        for (final RegenBlockBlock b : cloneBlockQueue) {
            if (b.getRegionName().equalsIgnoreCase(regionName)) {
                b.setRespawnTime(time);
                this.plugin.getConfiguration().setBlock(b.getLocation(), b.getType(), b.getData(), regionName, time);
            }
        }
    }
    
    public void updateRegionRespawnTime(final String regionName, final long respawnTime) {
        final Vector<RegenBlockBlock> cloneBlockQueue = (Vector<RegenBlockBlock>)this.blockQueue.clone();
        for (final RegenBlockBlock rb : cloneBlockQueue) {
            if (rb.getRegionName().equalsIgnoreCase(regionName)) {
                rb.setRespawnTime(respawnTime);
                this.plugin.getConfiguration().setBlock(rb.getLocation(), rb.getType(), rb.getData(), regionName, respawnTime);
            }
        }
    }
    
    public long shiftRegionRespawnTime(final String regionName, final long respawnTime) {
        final Vector<RegenBlockBlock> cloneBlockQueue = (Vector<RegenBlockBlock>)this.blockQueue.clone();
        final long time = System.currentTimeMillis();
        long earliestRespawn = 99999999999999L;
        long latestRespawn = 0L;
        for (final RegenBlockBlock rb : cloneBlockQueue) {
            if (rb.getRegionName().equalsIgnoreCase(regionName) && rb.getRespawnTime() > time) {
                if (earliestRespawn > rb.getRespawnTime()) {
                    earliestRespawn = rb.getRespawnTime();
                }
                if (latestRespawn >= rb.getRespawnTime()) {
                    continue;
                }
                latestRespawn = rb.getRespawnTime();
            }
        }
        if (earliestRespawn == 99999999999999L) {
            return 0L;
        }
        final long dt = respawnTime - earliestRespawn;
        for (final RegenBlockBlock rb2 : cloneBlockQueue) {
            if (rb2.getRegionName().equalsIgnoreCase(regionName) && rb2.getRespawnTime() > time) {
                rb2.setRespawnTime(rb2.getRespawnTime() + dt);
                this.plugin.getConfiguration().setBlock(rb2.getLocation(), rb2.getType(), rb2.getData(), regionName, rb2.getRespawnTime());
            }
        }
        return latestRespawn + dt + dt;
    }
    
    public Long getRegionRespawnTime(final String regionName) {
        final Vector<RegenBlockBlock> cloneBlockQueue = (Vector<RegenBlockBlock>)this.blockQueue.clone();
        final long time = System.currentTimeMillis();
        for (final RegenBlockBlock rb : cloneBlockQueue) {
            if (rb.getRegionName().equalsIgnoreCase(regionName) && rb.getRespawnTime() > time) {
                return rb.getRespawnTime();
            }
        }
        return null;
    }
    
    public void checkQueue() {
        final ArrayList<RegenBlockBlock> respawnedBlocks = new ArrayList<RegenBlockBlock>();
        final long time = System.currentTimeMillis();
        final Vector<RegenBlockBlock> cloneBlockQueue = (Vector<RegenBlockBlock>)this.blockQueue.clone();
        for (final RegenBlockBlock block : cloneBlockQueue) {
            final long respawnTime = block.getRespawnTime();
            final int x = block.getLocation().getBlockX();
            final int y = block.getLocation().getBlockY();
            final int z = block.getLocation().getBlockZ();
            final Material type = block.getType();
            final BlockData data = block.getData();
            final String regionName = block.getRegionName();
            final int sync = block.getSync();
            final int alarmTime = block.getAlarmTime();
            final String worldName = block.getLocation().getWorld().getName();
            final int regionType = block.getRegionType();
            if (sync == 1 && alarmTime != 0 && respawnTime - alarmTime * 1000 < time) {
                if (!this.alarmWentOffAt.containsKey(regionName)) {
                    this.alarmWentOffAt.put(regionName, time);
                    Bukkit.getServer().getPluginManager().callEvent((Event)new RegenBlockEventAlarm(block.getAlarmRadius(), block.getAlarmMessage(), worldName, x, y, z));
                }
                else if (this.alarmWentOffAt.get(regionName) + block.getAlarmTime() * 1000 + 10000L < time) {
                    this.alarmWentOffAt.remove(regionName);
                }
            }
            if (this.readyForRegen(worldName, regionName, respawnTime, x, y, z, type, data, regionType)) {
                respawnedBlocks.add(block);
            }
        }
        if (respawnedBlocks.size() > 0) {
            for (final RegenBlockBlock block : respawnedBlocks) {
                this.blockQueue.remove(block);
            }
            Bukkit.getServer().getPluginManager().callEvent((Event)new RegenBlockEventRespawn(respawnedBlocks));
        }
        if (this.nextConfigSave < time) {
            this.nextConfigSave = time + this.configSaveDelta;
            Bukkit.getServer().getPluginManager().callEvent((Event)new RegenBlockEventConfigSave());
        }
    }
    
    private boolean readyForRegen(final String worldName, final String regionName, final long respawnTime, final int x, final int y, final int z, final Material type, final BlockData data, final int regionType) {
        final long time = System.currentTimeMillis();
        if (time - respawnTime > 86400000L) {
            return true;
        }
        if (respawnTime < time && regionType == 0) {
            return (type) != Material.SAND && type != Material.GRAVEL || this.plugin.getServer().getWorld(worldName).getBlockAt(x, y - 1, z).getType() != Material.AIR;
        }
        return respawnTime < time && regionType == 1 && this.plugin.getServer().getWorld(worldName).getBlockAt(x, y - 1, z).getType() != Material.AIR;
    }
    
    public void broadcastMessage(final int regionAlarmRadius, final String regionAlarmMessage, final String worldName, final int x, final int y, final int z) {
        final Location location = new Location(this.plugin.getServer().getWorld(worldName), (double)x, (double)y, (double)z);
        Player[] onlinePlayers;
        for (int length = (onlinePlayers = this.plugin.getServer().getOnlinePlayers().toArray(new Player[0])).length, i = 0; i < length; ++i) {
            final Player player = onlinePlayers[i];
            if (player.getWorld().getName().equalsIgnoreCase(worldName) && player.getLocation().distance(location) <= regionAlarmRadius) {
                this.plugin.getLog().sendPlayerWarn(player, regionAlarmMessage);
            }
        }
    }
}
