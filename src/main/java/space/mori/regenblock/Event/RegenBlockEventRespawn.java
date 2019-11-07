package space.mori.regenblock.Event;

import org.bukkit.event.*;
import java.util.*;
import space.mori.regenblock.*;

public class RegenBlockEventRespawn extends Event
{
    private static final HandlerList handlers;
    private ArrayList<RegenBlockBlock> blocks;
    
    static {
        handlers = new HandlerList();
    }
    
    public RegenBlockEventRespawn(final ArrayList<RegenBlockBlock> blocks) {
        this.blocks = blocks;
    }
    
    public ArrayList<RegenBlockBlock> getBlocks() {
        return this.blocks;
    }
    
    public HandlerList getHandlers() {
        return RegenBlockEventRespawn.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return RegenBlockEventRespawn.handlers;
    }
}
