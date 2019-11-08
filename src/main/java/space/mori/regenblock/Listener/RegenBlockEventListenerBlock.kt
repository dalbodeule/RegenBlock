package space.mori.regenblock.listener

import org.bukkit.block.data.BlockData
import org.bukkit.event.entity.*
import org.bukkit.entity.*
import org.bukkit.event.block.*
import space.mori.regenblock.*
import org.bukkit.event.*
import org.bukkit.plugin.*
import space.mori.regenblock.event.*
import org.bukkit.*
import java.util.*
import java.util.regex.*
import kotlin.math.abs

class RegenBlockEventListenerBlock(private val plugin: RegenBlock) : Listener {
    private val rnd: Random = Random()
    private var lastRespawn: Long = 0
    private var respawnCounter: Long = 0

    init {
        this.lastRespawn = System.currentTimeMillis()
        this.respawnCounter = 0L
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEvent(event: BlockBreakEvent) {
        if (event.isCancelled || event.block.type == Material.AIR) {
            return
        }
        this.regenBlock(event.block.location, event.block.type, event.block.blockData, event.player, true)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEvent(event: BlockPlaceEvent) {
        if (event.isCancelled || event.blockPlaced.type == Material.AIR) {
            return
        }
        if (this.getBlockRegion(event.block.location) != null) {
            event.block.setType(event.block.type, false)
        }
        this.regenBlock(event.block.location, Material.AIR, Bukkit.createBlockData(Material.AIR), event.player, false)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEvent(event: EntityExplodeEvent) {
        if (event.isCancelled) {
            return
        }
        for (b in event.blockList()) {
            this.regenBlock(b.location, b.type, b.blockData, null, true)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEvent(event: BlockBurnEvent) {
        if (event.isCancelled || event.block.type == Material.FIRE) {
            return
        }
        val b = event.block
        this.regenBlock(b.location, b.type, b.blockData, null, true)
    }

    @EventHandler
    fun onEvent(event: RegenBlockEventAlarm) {
        this.plugin.queue!!.broadcastMessage(
            event.alarmRadius,
            event.alarmMessage,
            event.worldName,
            event.x,
            event.y,
            event.z
        )
    }

    @EventHandler
    fun onEvent(event: RegenBlockEventRespawn) {
        val blocks = event.blocks
        if (this.respawnCounter >= RegenBlockEventListenerBlock.maxBlocksPerRespawn && System.currentTimeMillis() - this.lastRespawn < RegenBlockEventListenerBlock.timeBetweenRespawn) {
            this.respawnCounter = 0L
            this.plugin.server.scheduler.scheduleSyncDelayedTask(
                this.plugin as Plugin,
                { Bukkit.getServer().pluginManager.callEvent(RegenBlockEventRespawn(blocks) as Event) },
                RegenBlockEventListenerBlock.tryAgainIn
            )
            return
        }
        var i = 0
        while (i < RegenBlockEventListenerBlock.maxBlocksPerRespawn && i < blocks.size) {
            val block = blocks[i]
            val b2 = block.location!!.world.getBlockAt(block.location)
            b2.setType(block.type, false)
            b2.setBlockData(block.data, false)
            this.plugin.configuration!!.removeBlock(b2)
            ++this.respawnCounter
            ++i
        }
        this.lastRespawn = System.currentTimeMillis()
        if (blocks.size > RegenBlockEventListenerBlock.maxBlocksPerRespawn) {
            blocks.subList(0, RegenBlockEventListenerBlock.maxBlocksPerRespawn - 1).clear()
            this.plugin.server.scheduler.scheduleSyncDelayedTask(
                this.plugin as Plugin,
                { Bukkit.getServer().pluginManager.callEvent(RegenBlockEventRespawn(blocks) as Event) },
                RegenBlockEventListenerBlock.tryAgainIn
            )
        }
    }

    @EventHandler
    fun onEvent(event: RegenBlockEventConfigSave) {
        if (this.plugin.configuration!!.isDirty) {
            this.plugin.configuration!!.saveBlocks()
        }
    }

    fun isPlayerEditor(playerName: String): Boolean {
        return this.plugin.listenerPlayer!!.playerEditStatus.contains(playerName)
    }

    fun regenBlock(location: Location, material: Material, data: BlockData, player: Player?, isBreakEvent: Boolean?) {
        var material = material
        var data = data
        if (player != null && this.isPlayerEditor(player.name)) {
            return
        }
        if (isBreakEvent!!) {
            if (!this.plugin.configuration!!.doMonitorBreak()) {
                return
            }
        } else if (!this.plugin.configuration!!.doMonitorPlace()) {
            return
        }
        if (this.plugin.configuration!!.listBlacklistBlock() != null && this.plugin.configuration!!.listBlacklistBlock()!!.contains(material.name)
        ) {
            return
        }
        if (material == Material.FIRE) {
            material = Material.AIR
        }
        val regionName = this.getBlockRegion(location)
        if (this.ignoreBlock(location, material, regionName)) {
            return
        }
        var respawnTime = this.plugin.configuration!!.getRegionRespawnTime(regionName!!)!!.toLong()
        respawnTime = System.currentTimeMillis() + respawnTime * 1000L
        val syncType = this.plugin.configuration!!.getRegionSync(regionName)
        val regionType = this.plugin.configuration!!.getRegionType(regionName)
        if (regionType == 1 && material != Material.AIR) {
            val spawnBlocksId = this.plugin.configuration!!.getRegionSpawnBlocks(regionName)
            if (spawnBlocksId!!.containsKey(material)) {
                var i = spawnBlocksId!!.keys.iterator()
                var totalChance = 0
                while (i.hasNext()) {
                    totalChance += spawnBlocksId!![i.next()]!!
                }
                if (totalChance == 0) {
                    return
                }
                val roll = this.rnd.nextInt(totalChance)
                var type = Material.AIR
                i = spawnBlocksId!!.keys.iterator()
                totalChance = 0
                while (i.hasNext()) {
                    val block = i.next()
                    val blockIdChance = spawnBlocksId!![block]
                    totalChance += blockIdChance!!
                    if (roll <= totalChance && roll >= totalChance - blockIdChance) {
                        type = block
                        break
                    }
                }
                material = type
                data = Bukkit.createBlockData(type)
            }
        }
        if (player != null && (this.plugin.configuration!!.getRegionFeedbackID(regionName) == 1 && material == Material.AIR || this.plugin.configuration!!.getRegionFeedbackID(
                regionName
            ) == 2)
        ) {
            val pat = Pattern.compile("TIME")
            val mat = pat.matcher(this.plugin.configuration!!.feedbackString)
            this.plugin.log!!.sendPlayerWarn(
                player,
                mat.replaceAll(this.plugin.configuration!!.getRegionRespawnTime(regionName).toString())
            )
        }
        this.queueBlock(location, material, data, regionName, respawnTime, syncType!!)
    }

    fun getBlockRegion(location: Location): String? {
        if (this.plugin.configuration!!.listRegions() == null) {
            return null
        }
        for (regionName in this.plugin.configuration!!.listRegions()!!) {
            val worldName = this.plugin.configuration!!.getRegionWorldName(regionName)
            if (!location.world.name.equals(worldName, ignoreCase = true)) {
                continue
            }
            val leftX = this.plugin.configuration!!.getRegionLeftX(regionName)
            val leftY = this.plugin.configuration!!.getRegionLeftY(regionName)
            val leftZ = this.plugin.configuration!!.getRegionLeftZ(regionName)
            val rightX = this.plugin.configuration!!.getRegionRightX(regionName)
            val rightY = this.plugin.configuration!!.getRegionRightY(regionName)
            val rightZ = this.plugin.configuration!!.getRegionRightZ(regionName)
            if (abs(leftX!! - rightX!!) == abs(leftX - location.blockX) + abs(rightX - location.blockX) && abs(
                    leftY!! - rightY!!
                ) == abs(leftY - location.blockY) + abs(rightY - location.blockY) && abs(leftZ!! - rightZ!!) == abs(
                    leftZ - location.blockZ
                ) + abs(rightZ - location.blockZ)
            ) {
                return regionName
            }
        }
        return null
    }

    private fun ignoreBlock(location: Location, type: Material, regionName: String?): Boolean {
        return regionName == null || this.plugin.configuration!!.listRegionBlacklistBlock(regionName) != null && this.plugin.configuration!!.listRegionBlacklistBlock(
            regionName
        )!!.contains(type.name) ||
                this.plugin.configuration!!.getBlockToRegen(
                    this.plugin.configuration!!.getRegionWorldName(regionName)!!,
                    location
                ) != null
    }

    private fun queueBlock(
        location: Location,
        material: Material,
        data: BlockData,
        regionName: String,
        respawnTime: Long,
        syncType: Int
    ) {
        var respawnTime = respawnTime
        if (syncType == 1) {
            val rt = this.plugin.queue!!.getRegionRespawnTime(regionName)
            if (rt != null) {
                respawnTime = rt
            }
        } else if (syncType == 2) {
            this.plugin.queue!!.updateRegionRespawnTime(regionName, respawnTime)
        } else if (syncType == 3) {
            val delta = this.plugin.queue!!.shiftRegionRespawnTime(regionName, respawnTime)
            if (delta != 0L) {
                respawnTime = delta
            }
        }
        this.plugin.configuration!!.setBlock(location, material, data, regionName, respawnTime)
        this.plugin.queue!!.addBlock(
            RegenBlockBlock(
                location,
                material,
                data,
                regionName,
                respawnTime,
                this.plugin.configuration!!.getRegionAlarmTime(regionName)!!,
                this.plugin.configuration!!.getRegionSync(regionName)!!,
                this.plugin.configuration!!.getRegionAlarmRadius(regionName)!!,
                this.plugin.configuration!!.getRegionAlarmMessage(regionName),
                this.plugin.configuration!!.getRegionType(regionName)!!
            )
        )
    }

    companion object {
        private var maxBlocksPerRespawn: Int = 0
        private var tryAgainIn: Long = 0
        private var timeBetweenRespawn: Long = 0

        init {
            RegenBlockEventListenerBlock.maxBlocksPerRespawn = 500
            RegenBlockEventListenerBlock.tryAgainIn = 40L
            RegenBlockEventListenerBlock.timeBetweenRespawn = 2000L
        }
    }
}
