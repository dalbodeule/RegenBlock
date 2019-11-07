package space.mori.regenblock.Listener;

import space.mori.regenblock.*;
import org.bukkit.block.*;
import org.bukkit.event.block.*;
import org.bukkit.event.*;
import org.bukkit.event.hanging.*;
import org.bukkit.event.player.*;
import org.bukkit.entity.*;

public class RegenBlockEventListenerCancel implements Listener
{
    private RegenBlock plugin;
    
    public RegenBlockEventListenerCancel(final RegenBlock plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEvent(final BlockFadeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.getBlockRegion(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEvent(final BlockFromToEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.getBlockRegion(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEvent(final BlockGrowEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.getBlockRegion(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEvent(final BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.getBlockRegion(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEvent(final BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Block block = event.getBlock();
        final String region = this.getBlockRegion(block);
        if (region != null) {
            switch (block.getType()) {
                case SAND:
                case GRAVEL:
                case TORCH:
                case LEGACY_SIGN_POST:
                case LEGACY_WALL_SIGN:
                case LEGACY_IRON_DOOR_BLOCK:
                case LEGACY_REDSTONE_TORCH_OFF:
                case LEGACY_REDSTONE_TORCH_ON:
                case LEGACY_SIGN:
                case LEGACY_WOOD_DOOR:
                case IRON_DOOR:
                case LEGACY_BED: {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onEvent(final LeavesDecayEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.getBlockRegion(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(final HangingPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.plugin.getListenerBlock().isPlayerEditor(event.getPlayer().getName())) {
            return;
        }
        if (this.getBlockRegion(event.getEntity().getLocation().getBlock()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(final HangingBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.getBlockRegion(event.getEntity().getLocation().getBlock()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEvent(final PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (this.plugin.getListenerBlock().isPlayerEditor(event.getPlayer().getName())) {
            return;
        }
        if (this.getBlockRegion(event.getRightClicked().getLocation().getBlock()) != null && (event.getRightClicked().getType() == EntityType.ITEM_FRAME || event.getRightClicked().getType() == EntityType.PAINTING)) {
            event.setCancelled(true);
        }
    }
    
    public String getBlockRegion(final Block block) {
        if (this.plugin.getConfiguration().listRegions() == null) {
            return null;
        }
        for (final String regionName : this.plugin.getConfiguration().listRegions()) {
            final String worldName = this.plugin.getConfiguration().getRegionWorldName(regionName);
            if (!block.getWorld().getName().equalsIgnoreCase(worldName)) {
                continue;
            }
            final int leftX = this.plugin.getConfiguration().getRegionLeftX(regionName);
            final int leftY = this.plugin.getConfiguration().getRegionLeftY(regionName);
            final int leftZ = this.plugin.getConfiguration().getRegionLeftZ(regionName);
            final int rightX = this.plugin.getConfiguration().getRegionRightX(regionName);
            final int rightY = this.plugin.getConfiguration().getRegionRightY(regionName);
            final int rightZ = this.plugin.getConfiguration().getRegionRightZ(regionName);
            if (Math.abs(leftX - rightX) == Math.abs(leftX - block.getX()) + Math.abs(rightX - block.getX()) && Math.abs(leftY - rightY) == Math.abs(leftY - block.getY()) + Math.abs(rightY - block.getY()) && Math.abs(leftZ - rightZ) == Math.abs(leftZ - block.getZ()) + Math.abs(rightZ - block.getZ())) {
                return regionName;
            }
        }
        return null;
    }
}
