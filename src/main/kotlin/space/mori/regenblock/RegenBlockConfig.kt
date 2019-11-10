package space.mori.regenblock

import org.bukkit.block.data.BlockData
import org.bukkit.configuration.file.*
import java.io.*
import org.bukkit.configuration.*
import org.bukkit.*
import org.bukkit.block.*
import java.util.*

class RegenBlockConfig(private val plugin: RegenBlock, private val configFile: File, private val blocksFile: File) {
    private val config: YamlConfiguration = YamlConfiguration()
    private val blocks: YamlConfiguration = YamlConfiguration()
    private val configDefaultsHash: HashMap<String, Any> = HashMap()
    var isDirty: Boolean = false
        private set

    val regionDefaultRespawnTime: Int
        get() = this.config.getInt("settings.defaultSpawnTime")

    var feedbackString: String
        get() = this.config.getString("settings.feedbackString")
        set(feedbackString) {
            this.config.set("settings.feedbackString", feedbackString as Any)
            this.save()
        }

    private fun setDirty() {
        this.isDirty = true
    }

    init {
        this.configDefaultsHash["settings.defaultSpawnTime"] = 5
        this.configDefaultsHash["settings.monitorPlace"] = true
        this.configDefaultsHash["settings.monitorBreak"] = true
        this.configDefaultsHash["settings.feedbackString"] =
            "This block will be restored to its original state in TIMEs."
        this.configDefaultsHash["settings.defaultSpawnBlocks.${Material.STONE}"] = 80
        this.configDefaultsHash["settings.defaultSpawnBlocks.${Material.IRON_ORE}"] = 15
        this.configDefaultsHash["settings.defaultSpawnBlocks.${Material.GOLD_ORE}"] = 5
        this.reload()
        this.reloadBlocks()
    }

    fun save() {
        try {
            this.config.save(this.configFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun saveBlocks() {
        try {
            this.blocks.save(this.blocksFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun load() {
        try {
            this.config.load(this.configFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e2: IOException) {
            e2.printStackTrace()
        } catch (e3: InvalidConfigurationException) {
            e3.printStackTrace()
        }

    }

    private fun loadBlocks() {
        try {
            this.blocks.load(this.blocksFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e2: IOException) {
            e2.printStackTrace()
        } catch (e3: InvalidConfigurationException) {
            e3.printStackTrace()
        }

    }

    fun reload() {
        if (this.configFile.exists()) {
            this.load()
            if (this.config.getString("settings.feedbackString") == null) {
                this.config.set("settings.feedbackString", this.configDefaultsHash["settings.feedbackString"])
            }
            if (this.config.getString("settings.defaultSpawnTime") == null) {
                this.config.set("settings.defaultSpawnTime", this.configDefaultsHash["settings.defaultSpawnTime"])
            }
            if (this.config.getString("settings.monitorBreak") == null) {
                this.config.set("settings.monitorBreak", this.configDefaultsHash["settings.monitorBreak"])
            }
            if (this.config.getString("settings.monitorPlace") == null) {
                this.config.set("settings.monitorPlace", this.configDefaultsHash["settings.monitorPlace"])
            }
            if (this.config.getString("settings.defaultSpawnBlocks") == null) {
                this.config.set(
                    "settings.defaultSpawnBlocks.${Material.STONE}",
                    this.configDefaultsHash["settings.defaultSpawnBlocks.${Material.STONE}"]
                )
                this.config.set(
                    "settings.defaultSpawnBlocks.${Material.IRON_ORE}",
                    this.configDefaultsHash["settings.defaultSpawnBlocks.${Material.IRON_ORE}"]
                )
                this.config.set(
                    "settings.defaultSpawnBlocks.${Material.GOLD_ORE}",
                    this.configDefaultsHash["settings.defaultSpawnBlocks.${Material.GOLD_ORE}"]
                )
            }
            this.save()
        } else {
            for (key in this.configDefaultsHash.keys) {
                this.config.set(key, this.configDefaultsHash[key])
            }
            this.save()
        }
    }

    fun reloadBlocks() {
        if (this.blocksFile.exists()) {
            this.loadBlocks()
        } else {
            this.saveBlocks()
        }
    }

    fun doMonitorPlace(): Boolean {
        return this.config.getBoolean("settings.monitorPlace")
    }

    fun doMonitorBreak(): Boolean {
        return this.config.getBoolean("settings.monitorBreak")
    }

    fun setMonitorPlace(`val`: Boolean) {
        this.config.set("settings.monitorPlace", `val` as Any)
    }

    fun setMonitorBreak(`val`: Boolean) {
        this.config.set("settings.monitorBreak", `val` as Any)
    }

    fun setRegion(regionName: String, respawnTime: Int, right: Location, left: Location) {
        this.config.set("region.$regionName.respawnTime", respawnTime as Any)
        this.config.set("region.$regionName.left.X", left.blockX as Any)
        this.config.set("region.$regionName.left.Y", left.blockY as Any)
        this.config.set("region.$regionName.left.Z", left.blockZ as Any)
        this.config.set("region.$regionName.right.X", right.blockX as Any)
        this.config.set("region.$regionName.right.Y", right.blockY as Any)
        this.config.set("region.$regionName.right.Z", right.blockZ as Any)
        this.config.set("region.$regionName.world", right.world.name as Any)
        this.config.set("region.$regionName.feedbackID", 0 as Any)
        this.config.set("region.$regionName.type", 0 as Any)
        this.config.set("region.$regionName.sync", 0 as Any)
        this.config.set("region.$regionName.alarmTime", 0 as Any)
        this.config.set("region.$regionName.alarmRadius", 100 as Any)
        this.config.set("region.$regionName.alarmMessage", "This area is about to respawn. Please move away." as Any)
        this.save()
    }

    fun removeRegion(regionName: String) {
        this.config.set("region.$regionName", null as Any?)
        this.save()
    }

    fun setBlock(location: Location, type: Material, data: BlockData, regionName: String, respawnTime: Long) {
        val blockName = "x" + location.blockX + "y" + location.blockY + "z" + location.blockZ
        val worldName = location.world.name
        this.blocks.set("blocksToRegen.$worldName.$blockName.X", location.blockX)
        this.blocks.set("blocksToRegen.$worldName.$blockName.Y", location.blockY)
        this.blocks.set("blocksToRegen.$worldName.$blockName.Z", location.blockZ)
        this.blocks.set("blocksToRegen.$worldName.$blockName.Type", type)
        this.blocks.set("blocksToRegen.$worldName.$blockName.Data", data)
        this.blocks.set("blocksToRegen.$worldName.$blockName.RegionName", regionName)
        this.blocks.set("blocksToRegen.$worldName.$blockName.RespawnTime", respawnTime)
        this.setDirty()
    }

    fun removeBlock(block: Block) {
        val blockName = "x" + block.x + "y" + block.y + "z" + block.z
        this.blocks.set("blocksToRegen." + block.world.name + "." + blockName, null)
        if (this.blocks.getString("blocksToRegen." + block.world.name) === "{}") {
            this.blocks.set("blocksToRegen." + block.world.name, null)
        }
        this.setDirty()
    }

    fun getBlockX(worldName: String, blockName: String): Int? {
        return this.blocks.getInt("blocksToRegen.$worldName.$blockName.X")
    }

    fun getBlockY(worldName: String, blockName: String): Int? {
        return this.blocks.getInt("blocksToRegen.$worldName.$blockName.Y")
    }

    fun getBlockZ(worldName: String, blockName: String): Int? {
        return this.blocks.getInt("blocksToRegen.$worldName.$blockName.Z")
    }

    fun getBlockType(worldName: String, blockName: String): Material? {
        return this.blocks.get("blocksToRegen.$worldName.$blockName.Type") as Material
    }

    fun getBlockData(worldName: String, blockName: String): BlockData? {
        return this.blocks.get("blocksToRegen.$worldName.$blockName.Data") as BlockData
    }

    fun getBlockRegionName(worldName: String, blockName: String): String? {
        return this.blocks.getString("blocksToRegen.$worldName.$blockName.RegionName")
    }

    fun getBlockRespawnTime(worldName: String, blockName: String): Long? {
        return this.blocks.getLong("blocksToRegen.$worldName.$blockName.RespawnTime")
    }

    fun getBlockToRegen(worldName: String, location: Location): String? {
        return this.blocks.getString("blocksToRegen.$worldName.x${location.blockX}y${location.blockY}z${location.blockZ}")
    }

    fun getRegionWorldName(regionName: String): String? {
        return this.config.getString("region.$regionName.world")
    }

    fun getRegionName(regionName: String): String? {
        return this.config.getString("region.$regionName")
    }

    fun setRegionRespawnTime(regionName: String, respawnTime: Int) {
        this.config.set("region.$regionName.respawnTime", respawnTime as Any)
    }

    fun getRegionRespawnTime(regionName: String): Int? {
        return this.config.getInt("region.$regionName.respawnTime", 0)
    }

    fun getRegionFeedbackID(regionName: String): Int? {
        return this.config.getInt("region.$regionName.feedbackID")
    }

    fun setRegionFeedbackID(regionName: String, feedback: Int): Int {
        var feedbackId = feedback
        if (feedbackId < 0 || feedbackId > 2) {
            feedbackId = 0
        }
        this.config.set("region.$regionName.feedbackID", feedbackId as Any)
        this.save()
        return feedbackId
    }

    fun getRegionLeft(regionName: String): String? {
        return this.config.getString("region.$regionName.left.X") + " " +
            this.config.getString("region.$regionName.left.Y") + " " +
            this.config.getString("region.$regionName.left.Z")
    }

    fun getRegionLeftX(regionName: String): Int? {
        return this.config.getInt("region.$regionName.left.X")
    }

    fun getRegionLeftY(regionName: String): Int? {
        return this.config.getInt("region.$regionName.left.Y")
    }

    fun getRegionLeftZ(regionName: String): Int? {
        return this.config.getInt("region.$regionName.left.Z")
    }

    fun getRegionRight(regionName: String): String? {
        return this.config.getString("region.$regionName.right.X") + " " +
            this.config.getString("region.$regionName.right.Y") + " " +
            this.config.getString("region.$regionName.right.Z")
    }

    fun getRegionRightX(regionName: String): Int? {
        return this.config.getInt("region.$regionName.right.X")
    }

    fun getRegionRightY(regionName: String): Int? {
        return this.config.getInt("region.$regionName.right.Y")
    }

    fun getRegionRightZ(regionName: String): Int? {
        return this.config.getInt("region.$regionName.right.Z")
    }

    fun listRegionBlacklistBlock(regionName: String): Set<String>? {
        return this.list("region.$regionName.blacklist.Type")
    }

    fun addRegionBlacklistBlock(regionName: String, block: Material) {
        this.config.set("region.$regionName.blacklist.Type.${block.name}", block.name)
        this.save()
    }

    fun removeRegionBlacklistBlock(regionName: String, block: Material) {
        this.config.set("region.$regionName.blacklist.Type.${block.name}", null as Any?)
        this.save()
    }

    fun setRegionType(regionName: String, type: Int) {
        this.config.set("region.$regionName.type", type as Any)
        this.save()
    }

    fun getRegionType(regionName: String): Int? {
        return this.config.getInt("region.$regionName.type", 0)
    }

    fun getRegionSync(regionName: String): Int? {
        return this.config.getInt("region.$regionName.sync", 0)
    }

    fun setRegionSync(regionName: String, sync: Int) {
        this.config.set("region.$regionName.sync", sync as Any)
        this.save()
    }

    fun getRegionSpawnBlocks(regionName: String): HashMap<Material, Int>? {
        val spawnBlocks = HashMap<Material, Int>()
        val spawnBlocksId = this.list("region.$regionName.spawnBlocks")
        if (spawnBlocksId != null) {
            for (spawnBlockId in spawnBlocksId) {
                spawnBlocks[Material.getMaterial(spawnBlockId)] =
                    this.config.getInt("region.$regionName.spawnBlocks.$spawnBlockId")
            }
        }
        return spawnBlocks
    }

    fun setRegionSpawnBlock(regionName: String, type: Material, chance: Int) {
        this.config.set("region.$regionName.spawnBlocks.${type.name}", chance as Any)
        this.save()
    }

    fun getRegionSpawnBlock(regionName: String, type: Material): Int? {
        return this.config.getInt("region.$regionName.spawnBlocks.${type.name}")
    }

    fun removeRegionSpawnBlock(regionName: String, type: Material) {
        this.config.set("region.$regionName.spawnBlocks.${type.name}", null as Any?)
        this.save()
    }

    fun regionAddSpawnBlocks(regionName: String) {
        if (this.config.getString("region.$regionName.spawnBlocks") == null) {
            val defaultSpawnBlocks = this.list("settings.defaultSpawnBlocks")
            if (defaultSpawnBlocks != null) {
                for (type in defaultSpawnBlocks) {
                    plugin.logger.info(type)
                    this.setRegionSpawnBlock(
                        regionName,
                        Material.getMaterial(type),
                        this.config.getInt("settings.defaultSpawnBlocks.$type", 50)
                    )
                }
            }
        }
    }

    fun getRegionAlarmTime(regionName: String): Int? {
        return this.config.getInt("region.$regionName.alarmTime", 0)
    }

    fun setRegionAlarmTime(regionName: String, alarmTime: Int) {
        this.config.set("region.$regionName.alarmTime", alarmTime as Any)
        this.save()
    }

    fun getRegionAlarmRadius(regionName: String): Int? {
        return this.config.getInt("region.$regionName.alarmRadius", 0)
    }

    fun setRegionAlarmRadius(regionName: String, alarmRadius: Int) {
        this.config.set("region.$regionName.alarmRadius", alarmRadius as Any)
        this.save()
    }

    fun getRegionAlarmMessage(regionName: String): String? {
        return this.config.getString(
            "region.$regionName.alarmMessage",
            "This area is about to respawn. Please move away."
        )
    }

    fun setRegionAlarmMessage(regionName: String, alarmMessage: String) {
        this.config.set("region.$regionName.alarmMessage", alarmMessage as Any)
        this.save()
    }

    fun list(path: String): Set<String>? {
        return if (this.config.getConfigurationSection(path) != null && this.config.getConfigurationSection(path).getKeys(
                false
            ) != null
        ) {
            Objects.requireNonNull(this.config.getConfigurationSection(path)).getKeys(false)
        } else null
    }

    fun listB(path: String): Set<String>? {
        return if (this.blocks.getConfigurationSection(path) != null && this.blocks.getConfigurationSection(path).getKeys(
                false
            ) != null
        ) {
            Objects.requireNonNull(this.blocks.getConfigurationSection(path)).getKeys(false)
        } else null
    }

    fun listRegions(): Set<String>? {
        return this.list("region")
    }

    fun listWorldsToRegen(): Set<String>? {
        return this.listB("blocksToRegen")
    }

    fun listBlacklistBlock(): Set<String>? {
        return this.list("blacklist.Type")
    }

    fun listBlocksToRegen(worldName: String): Set<String>? {
        return this.listB("blocksToRegen.$worldName")
    }

    fun removeBlacklistBlock(block: Material) {
        this.config.set("blacklist.Type.${block.name}", null as Any?)
        this.save()
    }

    fun addBlacklistBlock(block: Material) {
        this.config.set("blacklist.Type.${block.name}", block.name as Any)
        this.save()
    }

    fun copyWorldRegions(worldFrom: String, worldTo: String): Boolean {
        if (this.listRegions() == null) {
            return false
        }
        for (regionName in this.listRegions()!!) {
            if (worldFrom.equals(this.getRegionWorldName(regionName), ignoreCase = true)) {
                this.config.set(
                    "region.$regionName-$worldTo",
                    this.config.getConfigurationSection("region.$regionName") as Any
                )
                this.config.set("region.$regionName-$worldTo.world", worldTo as Any)
            }
        }
        this.save()
        return true
    }

    fun removeWorldRegions(worldName: String): Boolean {
        if (this.listRegions() == null) {
            return false
        }
        for (regionName in this.listRegions()!!) {
            if (worldName.equals(this.getRegionWorldName(regionName), ignoreCase = true)) {
                this.config.set("region.$regionName", null as Any?)
            }
        }
        this.save()
        return true
    }

    fun requeue() {
        if (this.listWorldsToRegen() == null) {
            return
        }
        for (worldName in this.listWorldsToRegen()!!) {
            if (this.listBlocksToRegen(worldName) != null) {
                for (blockName in this.listBlocksToRegen(worldName)!!) {
                    val x = this.getBlockX(worldName, blockName)
                    val y = this.getBlockY(worldName, blockName)
                    val z = this.getBlockZ(worldName, blockName)
                    val type = this.getBlockType(worldName, blockName)
                    val data = this.getBlockData(worldName, blockName)
                    val regionName = this.getBlockRegionName(worldName, blockName)
                    val respawnTime = this.getBlockRespawnTime(worldName, blockName)
                    val message = this.getRegionAlarmMessage(regionName!!)
                    val alarmRadius = this.getRegionAlarmRadius(regionName)
                    val alarmTime = this.getRegionAlarmTime(regionName)
                    val sync = this.getRegionSync(regionName)
                    val regionType = this.getRegionType(regionName)
                    val location =
                        Location(this.plugin.server.getWorld(worldName), x!!.toDouble(), y!!.toDouble(), z!!.toDouble())
                    val block = RegenBlockBlock(
                        location,
                        type!!,
                        data,
                        regionName,
                        respawnTime!!,
                        alarmTime!!,
                        sync!!,
                        alarmRadius!!,
                        message,
                        regionType!!
                    )
                    this.plugin.queue!!.addBlock(block)
                }
            }
        }
    }
}
