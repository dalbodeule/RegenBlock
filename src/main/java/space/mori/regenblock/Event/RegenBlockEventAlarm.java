package space.mori.regenblock.Event;

import org.bukkit.event.*;

public class RegenBlockEventAlarm extends Event
{
    private static final HandlerList handlers;
    private String alarmMessage;
    private String worldName;
    private int alarmRadius;
    private int x;
    private int y;
    private int z;
    
    static {
        handlers = new HandlerList();
    }
    
    public RegenBlockEventAlarm(final int alarmRadius, final String alarmMessage, final String worldName, final int x, final int y, final int z) {
        this.alarmRadius = alarmRadius;
        this.alarmMessage = alarmMessage;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public String getAlarmMessage() {
        return this.alarmMessage;
    }
    
    public String getWorldName() {
        return this.worldName;
    }
    
    public int getAlarmRadius() {
        return this.alarmRadius;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public HandlerList getHandlers() {
        return RegenBlockEventAlarm.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return RegenBlockEventAlarm.handlers;
    }
}
