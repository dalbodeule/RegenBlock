package space.mori.regenblock.Event;

import org.bukkit.event.*;

public class RegenBlockEventConfigSave extends Event
{
    private static final HandlerList handlers;
    
    static {
        handlers = new HandlerList();
    }
    
    public HandlerList getHandlers() {
        return RegenBlockEventConfigSave.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return RegenBlockEventConfigSave.handlers;
    }
}
