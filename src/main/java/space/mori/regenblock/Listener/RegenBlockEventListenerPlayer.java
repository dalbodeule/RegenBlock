package space.mori.regenblock.Listener;

import space.mori.regenblock.*;
import org.bukkit.*;
import java.util.*;
import org.bukkit.event.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class RegenBlockEventListenerPlayer implements Listener
{
    private RegenBlock plugin;
    private HashMap<String, Location> playerSelectionLeft;
    private HashMap<String, Location> playerSelectionRight;
    private ArrayList<String> playerSelectionStatus;
    private ArrayList<String> playerEditStatus;
    
    public HashMap<String, Location> getPlayerSelectionLeft() {
        return this.playerSelectionLeft;
    }
    
    public HashMap<String, Location> getPlayerSelectionRight() {
        return this.playerSelectionRight;
    }
    
    public ArrayList<String> getPlayerSelectionStatus() {
        return this.playerSelectionStatus;
    }
    
    public ArrayList<String> getPlayerEditStatus() {
        return this.playerEditStatus;
    }
    
    public RegenBlockEventListenerPlayer(final RegenBlock plugin) {
        this.playerSelectionLeft = new HashMap<String, Location>();
        this.playerSelectionRight = new HashMap<String, Location>();
        this.playerSelectionStatus = new ArrayList<String>();
        this.playerEditStatus = new ArrayList<String>();
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEvent(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!this.playerSelectionStatus.contains(event.getPlayer().getName())) {
            return;
        }
        final Player player = event.getPlayer();
        final Location loc = event.getClickedBlock().getLocation();
        final Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            final Location locOld = this.playerSelectionLeft.get(player.getName());
            if ((locOld != null && !locOld.equals((Object)loc)) || locOld == null) {
                this.playerSelectionLeft.put(player.getName(), loc);
                this.plugin.getLog().sendPlayerNormal(player, "Left Block: " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
            }
        }
        else if (action == Action.RIGHT_CLICK_BLOCK) {
            final Location locOld = this.playerSelectionRight.get(player.getName());
            if ((locOld != null && !locOld.equals((Object)loc)) || locOld == null) {
                this.playerSelectionRight.put(player.getName(), loc);
                this.plugin.getLog().sendPlayerNormal(player, "Right Block: " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
            }
        }
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onEvent(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        this.playerSelectionLeft.remove(player.getName());
        this.playerSelectionRight.remove(player.getName());
        this.playerSelectionStatus.remove(player.getName());
        this.playerEditStatus.remove(player.getName());
    }
    
    @EventHandler
    public void onEvent(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.playerSelectionLeft.remove(player.getName());
        this.playerSelectionRight.remove(player.getName());
        this.playerSelectionStatus.remove(player.getName());
        this.playerEditStatus.remove(player.getName());
    }
    
    @EventHandler
    public void onEvent(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        this.playerSelectionLeft.remove(player.getName());
        this.playerSelectionRight.remove(player.getName());
        this.playerSelectionStatus.remove(player.getName());
        this.playerEditStatus.remove(player.getName());
    }
}
