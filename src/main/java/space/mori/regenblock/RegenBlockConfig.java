package space.mori.regenblock;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.*;
import java.io.*;
import org.bukkit.configuration.*;
import org.bukkit.*;
import org.bukkit.block.*;
import java.util.*;

public class RegenBlockConfig
{
    private YamlConfiguration config;
    private YamlConfiguration blocks;
    private File configFile;
    private File blocksFile;
    private HashMap<String, Object> configDefaultsHash;
    private RegenBlock plugin;
    private boolean dirty;
    
    public boolean isDirty() {
        return this.dirty;
    }
    
    private void setDirty() {
        this.dirty = true;
    }
    
    public RegenBlockConfig(final RegenBlock plugin, final File configFile, final File blocksFile) {
        this.configDefaultsHash = new HashMap<String, Object>();
        this.plugin = plugin;
        this.config = new YamlConfiguration();
        this.blocks = new YamlConfiguration();
        this.configFile = configFile;
        this.blocksFile = blocksFile;
        this.configDefaultsHash.put("settings.defaultSpawnTime", 5);
        this.configDefaultsHash.put("settings.monitorPlace", true);
        this.configDefaultsHash.put("settings.monitorBreak", true);
        this.configDefaultsHash.put("settings.feedbackString", "This block will be restored to its original state in TIMEs.");
        this.configDefaultsHash.put("settings.defaultSpawnBlocks.1", 80);
        this.configDefaultsHash.put("settings.defaultSpawnBlocks.15", 15);
        this.configDefaultsHash.put("settings.defaultSpawnBlocks.14", 5);
        this.reload();
        this.reloadBlocks();
    }
    
    public void save() {
        try {
            this.config.save(this.configFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveBlocks() {
        try {
            this.blocks.save(this.blocksFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void load() {
        try {
            this.config.load(this.configFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        catch (InvalidConfigurationException e3) {
            e3.printStackTrace();
        }
    }
    
    private void loadBlocks() {
        try {
            this.blocks.load(this.blocksFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        catch (InvalidConfigurationException e3) {
            e3.printStackTrace();
        }
    }
    
    public void reload() {
        if (this.configFile.exists()) {
            this.load();
            if (this.config.getString("settings.feedbackString") == null) {
                this.config.set("settings.feedbackString", this.configDefaultsHash.get("settings.feedbackString"));
            }
            if (this.config.getString("settings.defaultSpawnTime") == null) {
                this.config.set("settings.defaultSpawnTime", this.configDefaultsHash.get("settings.defaultSpawnTime"));
            }
            if (this.config.getString("settings.monitorBreak") == null) {
                this.config.set("settings.monitorBreak", this.configDefaultsHash.get("settings.monitorBreak"));
            }
            if (this.config.getString("settings.monitorPlace") == null) {
                this.config.set("settings.monitorPlace", this.configDefaultsHash.get("settings.monitorPlace"));
            }
            if (this.config.getString("settings.defaultSpawnBlocks") == null) {
                this.config.set("settings.defaultSpawnBlocks.1", this.configDefaultsHash.get("settings.defaultSpawnBlocks.1"));
                this.config.set("settings.defaultSpawnBlocks.15", this.configDefaultsHash.get("settings.defaultSpawnBlocks.15"));
                this.config.set("settings.defaultSpawnBlocks.14", this.configDefaultsHash.get("settings.defaultSpawnBlocks.14"));
            }
            this.save();
        }
        else {
            for (final String key : this.configDefaultsHash.keySet()) {
                this.config.set(key, this.configDefaultsHash.get(key));
            }
            this.save();
        }
    }
    
    public void reloadBlocks() {
        if (this.blocksFile.exists()) {
            this.loadBlocks();
        }
        else {
            this.saveBlocks();
        }
    }
    
    public boolean doMonitorPlace() {
        return this.config.getBoolean("settings.monitorPlace");
    }
    
    public boolean doMonitorBreak() {
        return this.config.getBoolean("settings.monitorBreak");
    }
    
    public void setMonitorPlace(final boolean val) {
        this.config.set("settings.monitorPlace", (Object)val);
    }
    
    public void setMonitorBreak(final boolean val) {
        this.config.set("settings.monitorBreak", (Object)val);
    }
    
    public void setRegion(final String regionName, final int respawnTime, final Location right, final Location left) {
        this.config.set("region." + regionName + ".respawnTime", (Object)respawnTime);
        this.config.set("region." + regionName + ".left.X", (Object)left.getBlockX());
        this.config.set("region." + regionName + ".left.Y", (Object)left.getBlockY());
        this.config.set("region." + regionName + ".left.Z", (Object)left.getBlockZ());
        this.config.set("region." + regionName + ".right.X", (Object)right.getBlockX());
        this.config.set("region." + regionName + ".right.Y", (Object)right.getBlockY());
        this.config.set("region." + regionName + ".right.Z", (Object)right.getBlockZ());
        this.config.set("region." + regionName + ".world", (Object)right.getWorld().getName());
        this.config.set("region." + regionName + ".feedbackID", (Object)0);
        this.config.set("region." + regionName + ".type", (Object)0);
        this.config.set("region." + regionName + ".sync", (Object)0);
        this.config.set("region." + regionName + ".alarmTime", (Object)0);
        this.config.set("region." + regionName + ".alarmRadius", (Object)100);
        this.config.set("region." + regionName + ".alarmMessage", (Object)"This area is about to respawn. Please move away.");
        this.save();
    }
    
    public void removeRegion(final String regionName) {
        this.config.set("region." + regionName, (Object)null);
        this.save();
    }
    
    public void setBlock(final Location location, final Material type, final BlockData data, final String regionName, final long respawnTime) {
        final String blockName = "x" + location.getBlockX() + "y" + location.getBlockY() + "z" + location.getBlockZ();
        final String worldName = location.getWorld().getName();
        this.blocks.set("blocksToRegen." + worldName + "." + blockName + ".X", location.getBlockX());
        this.blocks.set("blocksToRegen." + worldName + "." + blockName + ".Y", location.getBlockY());
        this.blocks.set("blocksToRegen." + worldName + "." + blockName + ".Z", location.getBlockZ());
        this.blocks.set("blocksToRegen." + worldName + "." + blockName + ".Type", type);
        this.blocks.set("blocksToRegen." + worldName + "." + blockName + ".Data", data);
        this.blocks.set("blocksToRegen." + worldName + "." + blockName + ".RegionName", regionName);
        this.blocks.set("blocksToRegen." + worldName + "." + blockName + ".RespawnTime", respawnTime);
        this.setDirty();
    }
    
    public void removeBlock(final Block block) {
        final String blockName = "x" + block.getX() + "y" + block.getY() + "z" + block.getZ();
        this.blocks.set("blocksToRegen." + block.getWorld().getName() + "." + blockName, null);
        if (this.blocks.getString("blocksToRegen." + block.getWorld().getName()) == "{}") {
            this.blocks.set("blocksToRegen." + block.getWorld().getName(), null);
        }
        this.setDirty();
    }
    
    public int getBlockX(final String worldName, final String blockName) {
        return this.blocks.getInt("blocksToRegen." + worldName + "." + blockName + ".X");
    }
    
    public int getBlockY(final String worldName, final String blockName) {
        return this.blocks.getInt("blocksToRegen." + worldName + "." + blockName + ".Y");
    }
    
    public int getBlockZ(final String worldName, final String blockName) {
        return this.blocks.getInt("blocksToRegen." + worldName + "." + blockName + ".Z");
    }
    
    public Material getBlockType(final String worldName, final String blockName) {
        return (Material)this.blocks.get("blocksToRegen." + worldName + "." + blockName + ".Type");
    }
    
    public BlockData getBlockData(final String worldName, final String blockName) {
        return (BlockData)this.blocks.get("blocksToRegen." + worldName + "." + blockName + ".Data");
    }
    
    public String getBlockRegionName(final String worldName, final String blockName) {
        return this.blocks.getString("blocksToRegen." + worldName + "." + blockName + ".RegionName");
    }
    
    public long getBlockRespawnTime(final String worldName, final String blockName) {
        return this.blocks.getLong("blocksToRegen." + worldName + "." + blockName + ".RespawnTime");
    }
    
    public String getBlockToRegen(final String worldName, final Location location) {
        return this.blocks.getString("blocksToRegen." + worldName + "." + "x" + location.getBlockX() + "y" + location.getBlockY() + "z" + location.getBlockZ());
    }
    
    public String getRegionWorldName(final String regionName) {
        return this.config.getString("region." + regionName + ".world");
    }
    
    public String getRegionName(final String regionName) {
        return this.config.getString("region." + regionName);
    }
    
    public void setRegionRespawnTime(final String regionName, final int respawnTime) {
        this.config.set("region." + regionName + ".respawnTime", (Object)respawnTime);
    }
    
    public int getRegionRespawnTime(final String regionName) {
        return this.config.getInt("region." + regionName + ".respawnTime", 0);
    }
    
    public int getRegionDefaultRespawnTime() {
        return this.config.getInt("settings.defaultSpawnTime");
    }
    
    public int getRegionFeedbackID(final String regionName) {
        return this.config.getInt("region." + regionName + ".feedbackID");
    }
    
    public int setRegionFeedbackID(final String regionName, int feedbackId) {
        if (feedbackId < 0 || feedbackId > 2) {
            feedbackId = 0;
        }
        this.config.set("region." + regionName + ".feedbackID", (Object)feedbackId);
        this.save();
        return feedbackId;
    }
    
    public String getFeedbackString() {
        return this.config.getString("settings.feedbackString");
    }
    
    public void setFeedbackString(final String feedbackString) {
        this.config.set("settings.feedbackString", (Object)feedbackString);
        this.save();
    }
    
    public String getRegionLeft(final String regionName) {
        return this.config.getString(new StringBuilder("region.").append(regionName).append(".left.X").toString()) + " " + this.config.getString("region." + regionName + ".left.Y") + " " + this.config.getString("region." + regionName + ".left.Z");
    }
    
    public int getRegionLeftX(final String regionName) {
        return this.config.getInt("region." + regionName + ".left.X");
    }
    
    public int getRegionLeftY(final String regionName) {
        return this.config.getInt("region." + regionName + ".left.Y");
    }
    
    public int getRegionLeftZ(final String regionName) {
        return this.config.getInt("region." + regionName + ".left.Z");
    }
    
    public String getRegionRight(final String regionName) {
        return this.config.getString(new StringBuilder("region.").append(regionName).append(".right.X").toString()) + " " + this.config.getString("region." + regionName + ".right.Y") + " " + this.config.getString("region." + regionName + ".right.Z");
    }
    
    public int getRegionRightX(final String regionName) {
        return this.config.getInt("region." + regionName + ".right.X");
    }
    
    public int getRegionRightY(final String regionName) {
        return this.config.getInt("region." + regionName + ".right.Y");
    }
    
    public int getRegionRightZ(final String regionName) {
        return this.config.getInt("region." + regionName + ".right.Z");
    }
    
    public Set<String> listRegionBlacklistBlockId(final String regionName) {
        return this.list("region." + regionName + ".blacklist.TypeId");
    }
    
    public void addRegionBlacklistBlockId(final String regionName, final int id) {
        this.config.set("region." + regionName + ".blacklist.TypeId." + id, (Object)id);
        this.save();
    }
    
    public void removeRegionBlacklistBlockId(final String regionName, final int id) {
        this.config.set("region." + regionName + ".blacklist.TypeId." + id, (Object)null);
        this.save();
    }
    
    public void setRegionType(final String regionName, final int type) {
        this.config.set("region." + regionName + ".type", (Object)type);
        this.save();
    }
    
    public int getRegionType(final String regionName) {
        return this.config.getInt("region." + regionName + ".type", 0);
    }
    
    public int getRegionSync(final String regionName) {
        return this.config.getInt("region." + regionName + ".sync", 0);
    }
    
    public void setRegionSync(final String regionName, final int sync) {
        this.config.set("region." + regionName + ".sync", (Object)sync);
        this.save();
    }
    
    public HashMap<Material, Integer> getRegionSpawnBlocks(final String regionName) {
        final HashMap<Material, Integer> spawnBlocks = new HashMap<>();
        final Set<String> spawnBlocksId = this.list("region." + regionName + ".spawnBlocks");
        if (spawnBlocksId != null) {
            for (final String spawnBlockId : spawnBlocksId) {
                spawnBlocks.put(Material.getMaterial(spawnBlockId), this.config.getInt("region." + regionName + ".spawnBlocks." + spawnBlockId));
            }
        }
        return spawnBlocks;
    }
    
    public void setRegionSpawnBlock(final String regionName, final Material type, final int chance) {
        this.config.set("region." + regionName + ".spawnBlocks." + type, (Object)chance);
        this.save();
    }
    
    public int getRegionSpawnBlock(final String regionName, final Material type) {
        return this.config.getInt("region." + regionName + ".spawnBlocks." + type);
    }
    
    public void removeRegionSpawnBlock(final String regionName, final Material type) {
        this.config.set("region." + regionName + ".spawnBlocks." + type, (Object)null);
        this.save();
    }
    
    public void regionAddSpawnBlocks(final String regionName) {
        if (this.config.getString("region." + regionName + ".spawnBlocks") == null) {
            final Set<String> defaultSpawnBlocks = this.list("settings.defaultSpawnBlocks");
            if (defaultSpawnBlocks != null) {
                for (final String type : defaultSpawnBlocks) {
                    this.setRegionSpawnBlock(regionName, Material.getMaterial(type), this.config.getInt("settings.defaultSpawnBlocks." + type, 50));
                }
            }
        }
    }
    
    public int getRegionAlarmTime(final String regionName) {
        return this.config.getInt("region." + regionName + ".alarmTime", 0);
    }
    
    public void setRegionAlarmTime(final String regionName, final int alarmTime) {
        this.config.set("region." + regionName + ".alarmTime", (Object)alarmTime);
        this.save();
    }
    
    public int getRegionAlarmRadius(final String regionName) {
        return this.config.getInt("region." + regionName + ".alarmRadius", 0);
    }
    
    public void setRegionAlarmRadius(final String regionName, final int alarmRadius) {
        this.config.set("region." + regionName + ".alarmRadius", (Object)alarmRadius);
        this.save();
    }
    
    public String getRegionAlarmMessage(final String regionName) {
        return this.config.getString("region." + regionName + ".alarmMessage", "This area is about to respawn. Please move away.");
    }
    
    public void setRegionAlarmMessage(final String regionName, final String alarmMessage) {
        this.config.set("region." + regionName + ".alarmMessage", (Object)alarmMessage);
        this.save();
    }
    
    public Set<String> list(final String path) {
        if (this.config.getConfigurationSection(path) != null && this.config.getConfigurationSection(path).getKeys(false) != null) {
            return Objects.requireNonNull(this.config.getConfigurationSection(path)).getKeys(false);
        }
        return null;
    }
    
    public Set<String> listB(final String path) {
        if (this.blocks.getConfigurationSection(path) != null && this.blocks.getConfigurationSection(path).getKeys(false) != null) {
            return Objects.requireNonNull(this.blocks.getConfigurationSection(path)).getKeys(false);
        }
        return null;
    }
    
    public Set<String> listRegions() {
        return this.list("region");
    }
    
    public Set<String> listWorldsToRegen() {
        return this.listB("blocksToRegen");
    }
    
    public Set<String> listBlacklistBlockId() {
        return this.list("blacklist.TypeId");
    }
    
    public Set<String> listBlocksToRegen(final String worldName) {
        return this.listB("blocksToRegen." + worldName);
    }
    
    public void removeBlacklistBlockId(final int id) {
        this.config.set("blacklist.TypeId." + id, (Object)null);
        this.save();
    }
    
    public void addBlacklistBlockId(final int id) {
        this.config.set("blacklist.TypeId." + id, (Object)id);
        this.save();
    }
    
    public boolean copyWorldRegions(final String worldFrom, final String worldTo) {
        if (this.listRegions() == null) {
            return false;
        }
        for (final String regionName : this.listRegions()) {
            if (worldFrom.equalsIgnoreCase(this.getRegionWorldName(regionName))) {
                this.config.set("region." + regionName + "-" + worldTo, (Object)this.config.getConfigurationSection("region." + regionName));
                this.config.set("region." + regionName + "-" + worldTo + ".world", (Object)worldTo);
            }
        }
        this.save();
        return true;
    }
    
    public boolean removeWorldRegions(final String worldName) {
        if (this.listRegions() == null) {
            return false;
        }
        for (final String regionName : this.listRegions()) {
            if (worldName.equalsIgnoreCase(this.getRegionWorldName(regionName))) {
                this.config.set("region." + regionName, (Object)null);
            }
        }
        this.save();
        return true;
    }
    
    public void requeue() {
        if (this.listWorldsToRegen() == null) {
            return;
        }
        for (final String worldName : this.listWorldsToRegen()) {
            if (this.listBlocksToRegen(worldName) != null) {
                for (final String blockName : this.listBlocksToRegen(worldName)) {
                    final int x = this.getBlockX(worldName, blockName);
                    final int y = this.getBlockY(worldName, blockName);
                    final int z = this.getBlockZ(worldName, blockName);
                    final Material type = this.getBlockType(worldName, blockName);
                    final BlockData data = this.getBlockData(worldName, blockName);
                    final String regionName = this.getBlockRegionName(worldName, blockName);
                    final long respawnTime = this.getBlockRespawnTime(worldName, blockName);
                    final String message = this.getRegionAlarmMessage(regionName);
                    final int alarmRadius = this.getRegionAlarmRadius(regionName);
                    final int alarmTime = this.getRegionAlarmTime(regionName);
                    final int sync = this.getRegionSync(regionName);
                    final int regionType = this.getRegionType(regionName);
                    final Location location = new Location(this.plugin.getServer().getWorld(worldName), (double)x, (double)y, (double)z);
                    final RegenBlockBlock block = new RegenBlockBlock(location, type, data, regionName, respawnTime, alarmTime, sync, alarmRadius, message, regionType);
                    this.plugin.getQueue().addBlock(block);
                }
            }
        }
    }
}
