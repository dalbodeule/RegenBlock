package space.mori.regenblock

import org.bukkit.block.data.BlockData
import org.bukkit.plugin.java.*
import java.io.*

import space.mori.regenblock.command.RegenBlockCommandExecutor
import space.mori.regenblock.listener.*
import org.bukkit.*
import org.bukkit.entity.*

class RegenBlock(
    private var commandExecutor: RegenBlockCommandExecutor? = null,
    var log: RegenBlockLogHandler? = null,
    var configuration: RegenBlockConfig? = null,
    private var pluginPath: String? = null,
    private var configFile: File? = null,
    private var blocksFile: File? = null,
    var listenerBlock: RegenBlockEventListenerBlock? = null,
    var listenerPlayer: RegenBlockEventListenerPlayer? = null,
    private var listenerCancel: RegenBlockEventListenerCancel? = null,
    var queue: RegenBlockQueue? = null
) : JavaPlugin() {
    companion object {
        lateinit var instance: RegenBlock
    }
    override fun onEnable() {
        instance = this

        this.log = RegenBlockLogHandler(this)
        this.listenerBlock = RegenBlockEventListenerBlock(this)
        this.listenerPlayer = RegenBlockEventListenerPlayer(this)
        this.listenerCancel = RegenBlockEventListenerCancel(this)

        val pm = this.server.pluginManager

        pm.registerEvents(this.listenerBlock, this)
        pm.registerEvents(this.listenerPlayer, this)
        pm.registerEvents(this.listenerCancel, this)

        this.pluginPath = this.dataFolder.absolutePath
        this.configFile = File(this.pluginPath + File.separator + "config.yml")
        this.blocksFile = File(this.pluginPath + File.separator + "blocks.yml")
        this.configuration = RegenBlockConfig(this, this.configFile!!, this.blocksFile!!)
        this.commandExecutor = RegenBlockCommandExecutor(this)

        this.getCommand("rb").executor = this.commandExecutor

        this.queue = RegenBlockQueue(this)

        this.server.scheduler.runTaskAsynchronously(this, queue)

        this.server.scheduler.scheduleSyncDelayedTask(this, { this@RegenBlock.configuration!!.requeue() }, 200L)
    }

    override fun onDisable() {
        this.configuration!!.save()
        this.configuration!!.saveBlocks()
    }

    fun regenBlock(location: Location, material: Material, data: BlockData, player: Player, isBreakEvent: Boolean?) {
        this.listenerBlock!!.regenBlock(location, material, data, player, isBreakEvent)
    }

    fun copyWorldRegion(worldFrom: String, worldTo: String): Boolean {
        return this.configuration!!.copyWorldRegions(worldFrom, worldTo)
    }

    fun removeWorldRegions(worldName: String): Boolean {
        return this.configuration!!.removeWorldRegions(worldName)
    }
}
