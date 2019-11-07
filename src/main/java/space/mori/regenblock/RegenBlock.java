package space.mori.regenblock;

import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.*;
import java.io.*;
import space.mori.regenblock.Listener.*;
import org.bukkit.event.*;
import org.bukkit.command.*;
import org.bukkit.plugin.*;
import org.bukkit.*;
import org.bukkit.entity.*;

public class RegenBlock extends JavaPlugin
{
    private RegenBlockCommandExecutor commandExecutor;
    private RegenBlockLogHandler log;
    private RegenBlockConfig config;
    private String pluginPath;
    private File configFile;
    private File blocksFile;
    private RegenBlockEventListenerBlock listenerBlock;
    private RegenBlockEventListenerPlayer listenerPlayer;
    private RegenBlockEventListenerCancel listenerCancel;
    private RegenBlockQueue queue;
    
    public RegenBlockLogHandler getLog() {
        return this.log;
    }
    
    public RegenBlockConfig getConfiguration() {
        return this.config;
    }
    
    public RegenBlockEventListenerBlock getListenerBlock() {
        return this.listenerBlock;
    }
    
    public RegenBlockEventListenerPlayer getListenerPlayer() {
        return this.listenerPlayer;
    }
    
    public RegenBlockQueue getQueue() {
        return this.queue;
    }
    
    public void onEnable() {
        this.log = new RegenBlockLogHandler(this);
        this.listenerBlock = new RegenBlockEventListenerBlock(this);
        this.listenerPlayer = new RegenBlockEventListenerPlayer(this);
        this.listenerCancel = new RegenBlockEventListenerCancel(this);
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents((Listener)this.listenerBlock, (Plugin)this);
        pm.registerEvents((Listener)this.listenerPlayer, (Plugin)this);
        pm.registerEvents((Listener)this.listenerCancel, (Plugin)this);
        this.pluginPath = this.getDataFolder().getAbsolutePath();
        this.configFile = new File(String.valueOf(this.pluginPath) + File.separator + "config.yml");
        this.blocksFile = new File(String.valueOf(this.pluginPath) + File.separator + "blocks.yml");
        this.config = new RegenBlockConfig(this, this.configFile, this.blocksFile);
        this.commandExecutor = new RegenBlockCommandExecutor(this);
        this.getCommand("rb").setExecutor((CommandExecutor)this.commandExecutor);
        this.queue = new RegenBlockQueue(this);
        new Thread(this.queue).start();
        this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                RegenBlock.this.config.requeue();
            }
        }, 200L);
    }
    
    public void onDisable() {
        this.config.save();
        this.config.saveBlocks();
    }
    
    public void regenBlock(final Location location, final Material material, final BlockData data, final Player player, final Boolean isBreakEvent) {
        this.listenerBlock.regenBlock(location, material, data, player, isBreakEvent);
    }
    
    public boolean copyWorldRegion(final String worldFrom, final String worldTo) {
        return this.config.copyWorldRegions(worldFrom, worldTo);
    }
    
    public boolean removeWorldRegions(final String worldName) {
        return this.config.removeWorldRegions(worldName);
    }
}
