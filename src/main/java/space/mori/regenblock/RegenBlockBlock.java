package space.mori.regenblock;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;

public class RegenBlockBlock implements Comparable<RegenBlockBlock>
{
    private Location location;
    private Material type;
    private BlockData data;
    private String regionName;
    private long respawnTime;
    private int sync;
    private int alarmTime;
    private int alarmRadius;
    private String alarmMessage;
    private int regionType;
    
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(final Location location) {
        this.location = location;
    }
    
    public Material getType() {
        return this.type;
    }
    
    public void setTypeId(final Material type) {
        this.type = type;
    }
    
    public BlockData getData() {
        return this.data;
    }
    
    public void setData(final BlockData data) {
        this.data = data;
    }
    
    public String getRegionName() {
        return this.regionName;
    }
    
    public void setRegionName(final String regionName) {
        this.regionName = regionName;
    }
    
    public long getRespawnTime() {
        return this.respawnTime;
    }
    
    public void setRespawnTime(final long respawnTime) {
        this.respawnTime = respawnTime;
    }
    
    public int getAlarmTime() {
        return this.alarmTime;
    }
    
    public void setAlarmTime(final int alarmTime) {
        this.alarmTime = alarmTime;
    }
    
    public String getAlarmMessage() {
        return this.alarmMessage;
    }
    
    public void setAlarmMessage(final String alarmMessage) {
        this.alarmMessage = alarmMessage;
    }
    
    public int getSync() {
        return this.sync;
    }
    
    public void setSync(final int sync) {
        this.sync = sync;
    }
    
    public int getAlarmRadius() {
        return this.alarmRadius;
    }
    
    public void setAlarmRadius(final int alarmRadius) {
        this.alarmRadius = alarmRadius;
    }
    
    public int getRegionType() {
        return this.regionType;
    }
    
    public void setRegionType(final int regionType) {
        this.regionType = regionType;
    }
    
    public RegenBlockBlock(final Location location, final Material type, final BlockData data, final String regionName, final long respawnTime, final int alarmTime, final int sync, final int alarmRadius, final String alarmMessage, final int regionType) {
        this.location = location;
        this.type = type;
        this.data = data;
        this.regionName = regionName;
        this.respawnTime = respawnTime;
        this.alarmTime = alarmTime;
        this.sync = sync;
        this.alarmRadius = alarmRadius;
        this.alarmMessage = alarmMessage;
        this.regionType = regionType;
    }
    
    @Override
    public int compareTo(final RegenBlockBlock o) {
        if (this.respawnTime > o.respawnTime) {
            return -1;
        }
        if (this.respawnTime == o.respawnTime && this.location.getBlockX() > o.getLocation().getBlockX()) {
            return -1;
        }
        if (this.respawnTime == o.respawnTime && this.location.getBlockZ() > o.getLocation().getBlockZ()) {
            return -1;
        }
        if (this.respawnTime == o.respawnTime && this.location.getBlockY() > o.getLocation().getBlockY()) {
            return -1;
        }
        return 1;
    }
}
