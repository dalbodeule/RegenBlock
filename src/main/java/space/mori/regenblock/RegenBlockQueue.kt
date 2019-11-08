package space.mori.regenblock

import java.util.*

import org.bukkit.block.data.BlockData
import space.mori.regenblock.event.*
import org.bukkit.*
import org.bukkit.entity.*

class RegenBlockQueue(private val plugin: RegenBlock) : Runnable {
    private val queueDelta: Long
    private var nextConfigSave: Long = 0
    private val configSaveDelta: Long
    private val alarmWentOffAt: HashMap<String, Long>
    private val blockQueue: Vector<RegenBlockBlock>

    init {
        this.queueDelta = 1000L
        this.nextConfigSave = System.currentTimeMillis()
        this.configSaveDelta = 60000L
        this.alarmWentOffAt = HashMap()
        this.blockQueue = Vector()
    }

    override fun run() {
        while (true) {
            try {
                Thread.sleep(this.queueDelta)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            this.checkQueue()
        }
    }

    fun addBlock(block: RegenBlockBlock) {
        this.blockQueue.add(block)
    }

    fun regenRegion(regionName: String) {
        val time = System.currentTimeMillis()
        val cloneBlockQueue = this.blockQueue.clone() as Vector<RegenBlockBlock>
        for (b in cloneBlockQueue) {
            if (b.regionName!!.equals(regionName, ignoreCase = true)) {
                b.respawnTime = time
                this.plugin.configuration!!.setBlock(b.location!!, b.type!!, b.data!!, regionName, time)
            }
        }
    }

    fun updateRegionRespawnTime(regionName: String, respawnTime: Long) {
        val cloneBlockQueue = this.blockQueue.clone() as Vector<RegenBlockBlock>
        for (rb in cloneBlockQueue) {
            if (rb.regionName!!.equals(regionName, ignoreCase = true)) {
                rb.respawnTime = respawnTime
                this.plugin.configuration!!.setBlock(rb.location!!, rb.type!!, rb.data!!, regionName, respawnTime)
            }
        }
    }

    fun shiftRegionRespawnTime(regionName: String, respawnTime: Long): Long {
        val cloneBlockQueue = this.blockQueue.clone() as Vector<RegenBlockBlock>
        val time = System.currentTimeMillis()
        var earliestRespawn = 99999999999999L
        var latestRespawn = 0L
        for (rb in cloneBlockQueue) {
            if (rb.regionName!!.equals(regionName, ignoreCase = true) && rb.respawnTime > time) {
                if (earliestRespawn > rb.respawnTime) {
                    earliestRespawn = rb.respawnTime
                }
                if (latestRespawn >= rb.respawnTime) {
                    continue
                }
                latestRespawn = rb.respawnTime
            }
        }
        if (earliestRespawn == 99999999999999L) {
            return 0L
        }
        val dt = respawnTime - earliestRespawn
        for (rb2 in cloneBlockQueue) {
            if (rb2.regionName!!.equals(regionName, ignoreCase = true) && rb2.respawnTime > time) {
                rb2.respawnTime = rb2.respawnTime + dt
                this.plugin.configuration!!.setBlock(rb2.location!!, rb2.type!!, rb2.data!!, regionName, rb2.respawnTime)
            }
        }
        return latestRespawn + dt + dt
    }

    fun getRegionRespawnTime(regionName: String): Long? {
        val cloneBlockQueue = this.blockQueue.clone() as Vector<RegenBlockBlock>
        val time = System.currentTimeMillis()
        for (rb in cloneBlockQueue) {
            if (rb.regionName!!.equals(regionName, ignoreCase = true) && rb.respawnTime > time) {
                return rb.respawnTime
            }
        }
        return null
    }

    fun checkQueue() {
        val respawnedBlocks = ArrayList<RegenBlockBlock>()
        val time = System.currentTimeMillis()
        val cloneBlockQueue = this.blockQueue.clone() as Vector<RegenBlockBlock>
        for (block in cloneBlockQueue) {
            val respawnTime = block.respawnTime
            val x = block.location!!.blockX
            val y = block.location!!.blockY
            val z = block.location!!.blockZ
            val type = block.type
            val data = block.data
            val regionName = block.regionName
            val sync = block.sync
            val alarmTime = block.alarmTime
            val worldName = block.location!!.world.name
            val regionType = block.regionType
            if (sync == 1 && alarmTime != 0 && respawnTime - alarmTime * 1000 < time) {
                if (!this.alarmWentOffAt.containsKey(regionName)) {
                    this.alarmWentOffAt[regionName!!] = time
                    Bukkit.getScheduler().runTask(plugin) {
                        Bukkit.getPluginManager()
                            .callEvent(RegenBlockEventAlarm(block.alarmRadius, block.alarmMessage!!, worldName, x, y, z))
                    }
                } else if (this.alarmWentOffAt[regionName]!! + (block.alarmTime * 1000).toLong() + 10000L < time) {
                    this.alarmWentOffAt.remove(regionName)
                }
            }
            if (this.readyForRegen(worldName, regionName, respawnTime, x, y, z, type, data, regionType)) {
                respawnedBlocks.add(block)
            }
        }
        if (respawnedBlocks.size > 0) {
            for (block in respawnedBlocks) {
                this.blockQueue.remove(block)
            }
            Bukkit.getScheduler()
                .runTask(plugin) { Bukkit.getPluginManager().callEvent(RegenBlockEventRespawn(respawnedBlocks)) }
        }
        if (this.nextConfigSave < time) {
            this.nextConfigSave = time + this.configSaveDelta
            Bukkit.getScheduler().runTask(plugin) { Bukkit.getPluginManager().callEvent(RegenBlockEventConfigSave()) }
        }
    }

    private fun readyForRegen(
        worldName: String,
        regionName: String?,
        respawnTime: Long,
        x: Int,
        y: Int,
        z: Int,
        type: Material?,
        data: BlockData?,
        regionType: Int
    ): Boolean {
        val time = System.currentTimeMillis()
        if (time - respawnTime > 86400000L) {
            return true
        }
        return if (respawnTime < time && regionType == 0) {
            type != Material.SAND && type != Material.GRAVEL || this.plugin.server.getWorld(worldName).getBlockAt(
                x,
                y - 1,
                z
            ).type != Material.AIR
        } else respawnTime < time && regionType == 1 && this.plugin.server.getWorld(worldName).getBlockAt(
            x,
            y - 1,
            z
        ).type != Material.AIR
    }

    fun broadcastMessage(
        regionAlarmRadius: Int,
        regionAlarmMessage: String,
        worldName: String,
        x: Int,
        y: Int,
        z: Int
    ) {
        val location = Location(this.plugin.server.getWorld(worldName), x.toDouble(), y.toDouble(), z.toDouble())
        val onlinePlayers: Array<Player> = plugin.server.onlinePlayers.toTypedArray()
        val length = onlinePlayers.size
        var i = 0
        while (i < length) {
            val player = onlinePlayers[i]
            if (player.world.name.equals(
                    worldName,
                    ignoreCase = true
                ) && player.location.distance(location) <= regionAlarmRadius
            ) {
                this.plugin.log!!.sendPlayerWarn(player, regionAlarmMessage)
            }
            ++i
        }
    }
}
