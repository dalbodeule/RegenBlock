package space.mori.regenblock.listener

import org.bukkit.Material
import space.mori.regenblock.*
import org.bukkit.block.*
import org.bukkit.event.block.*
import org.bukkit.event.*
import org.bukkit.event.hanging.*
import org.bukkit.event.player.*
import org.bukkit.entity.*
import kotlin.math.abs

class RegenBlockEventListenerCancel(private val plugin: RegenBlock) : Listener {

    @EventHandler
    fun onEvent(event: BlockFadeEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.getBlockRegion(event.block) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEvent(event: BlockFromToEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.getBlockRegion(event.block) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEvent(event: BlockGrowEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.getBlockRegion(event.block) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEvent(event: BlockIgniteEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.getBlockRegion(event.block) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEvent(event: BlockPhysicsEvent) {
        if (event.isCancelled) {
            return
        }
        val block = event.block
        val region = this.getBlockRegion(block)
        if (region != null) {
            when (block.type) {
                Material.SAND,
                Material.GRAVEL,
                Material.TORCH,
                Material.WALL_SIGN,
                Material.SIGN,
                Material.DARK_OAK_DOOR,
                Material.ACACIA_DOOR,
                Material.BIRCH_DOOR,
                Material.JUNGLE_DOOR,
                Material.OAK_DOOR,
                Material.SPRUCE_DOOR,
                Material.IRON_DOOR,
                Material.BLUE_BED,
                Material.BROWN_BED,
                Material.CYAN_BED,
                Material.GRAY_BED,
                Material.GREEN_BED,
                Material.LIGHT_BLUE_BED,
                Material.LIGHT_GRAY_BED,
                Material.LIME_BED,
                Material.MAGENTA_BED,
                Material.ORANGE_BED,
                Material.PINK_BED,
                Material.PURPLE_BED,
                Material.RED_BED,
                Material.WHITE_BED,
                Material.YELLOW_BED,
                Material.BLACK_BED -> {
                    event.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onEvent(event: LeavesDecayEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.getBlockRegion(event.block) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEvent(event: HangingPlaceEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.plugin.listenerBlock!!.isPlayerEditor(event.player.name)) {
            return
        }
        if (this.getBlockRegion(event.entity.location.block) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEvent(event: HangingBreakEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.getBlockRegion(event.entity.location.block) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEvent(event: PlayerInteractEntityEvent) {
        if (event.isCancelled) {
            return
        }
        if (this.plugin.listenerBlock!!.isPlayerEditor(event.player.name)) {
            return
        }
        if (this.getBlockRegion(event.rightClicked.location.block) != null && (event.rightClicked.type == EntityType.ITEM_FRAME || event.rightClicked.type == EntityType.PAINTING)) {
            event.isCancelled = true
        }
    }

    fun getBlockRegion(block: Block): String? {
        if (this.plugin.configuration!!.listRegions() == null) {
            return null
        }
        for (regionName in this.plugin.configuration!!.listRegions()!!) {
            val worldName = this.plugin.configuration!!.getRegionWorldName(regionName)
            if (!block.world.name.equals(worldName, ignoreCase = true)) {
                continue
            }
            val leftX = this.plugin.configuration!!.getRegionLeftX(regionName)
            val leftY = this.plugin.configuration!!.getRegionLeftY(regionName)
            val leftZ = this.plugin.configuration!!.getRegionLeftZ(regionName)
            val rightX = this.plugin.configuration!!.getRegionRightX(regionName)
            val rightY = this.plugin.configuration!!.getRegionRightY(regionName)
            val rightZ = this.plugin.configuration!!.getRegionRightZ(regionName)
            if (abs(leftX!! - rightX!!) == abs(leftX - block.x) + abs(rightX - block.x) && abs(leftY!! - rightY!!) == abs(
                    leftY - block.y
                ) + abs(rightY - block.y) && abs(leftZ!! - rightZ!!) == abs(leftZ - block.z) + abs(
                    rightZ - block.z
                )
            ) {
                return regionName
            }
        }
        return null
    }
}
