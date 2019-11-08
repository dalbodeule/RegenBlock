package space.mori.regenblock

import java.util.logging.*
import org.bukkit.plugin.*
import org.bukkit.*
import org.bukkit.entity.*
import java.util.*

class RegenBlockLogHandler(private val plugin: RegenBlock) {
    private val logger: Logger = Logger.getLogger("Minecraft")

    private fun buildString(message: String): String {
        val pdFile = this.plugin.description
        return "[${pdFile.name}] (${pdFile.version}) $message"
    }

    private fun buildStringPlayer(message: String, color: ChatColor): String {
        val pdFile = this.plugin.description
        return "$color[${pdFile.name}] $message"
    }

    fun info(message: String) {
        this.logger.info(this.buildString(message))
    }

    fun warn(message: String) {
        this.logger.warning(this.buildString(message))
    }

    fun sendPlayerNormal(player: Player, message: String) {
        player.sendMessage(this.buildStringPlayer(message, ChatColor.AQUA))
    }

    fun sendPlayerWarn(player: Player, message: String) {
        player.sendMessage(this.buildStringPlayer(message, ChatColor.YELLOW))
    }

    fun sendPlayerRegionInfo(player: Player, regionName: String) {
        val left = this.plugin.configuration!!.getRegionLeft(regionName)
        val right = this.plugin.configuration!!.getRegionRight(regionName)
        val worldName = this.plugin.configuration!!.getRegionWorldName(regionName)
        val respawnTime = this.plugin.configuration!!.getRegionRespawnTime(regionName)
        val sync = this.plugin.configuration!!.getRegionSync(regionName)
        val type = this.plugin.configuration!!.getRegionType(regionName)
        val feedbackId = this.plugin.configuration!!.getRegionFeedbackID(regionName)
        val alarmTime = this.plugin.configuration!!.getRegionAlarmTime(regionName)
        val alarmRadius = this.plugin.configuration!!.getRegionAlarmRadius(regionName)
        val alarmMessage = this.plugin.configuration!!.getRegionAlarmMessage(regionName)
        this.sendPlayerNormal(
            player,
            "$regionName: [W] $worldName; [L] $left; [R] $right; [T] ${respawnTime}s."
        )
        this.sendPlayerNormal(player, "[Sync] $sync; [Type] $type; [Feedback ID] $feedbackId")
        this.sendPlayerNormal(player, "Alarm [Time] $alarmTime; [Radius] $alarmRadius; [Message] $alarmMessage")
    }

    fun listRegion(player: Player, listRegion: Set<String>) {
        for (regionName in listRegion) {
            val left = this.plugin.configuration!!.getRegionLeft(regionName)
            val right = this.plugin.configuration!!.getRegionRight(regionName)
            val worldName = this.plugin.configuration!!.getRegionWorldName(regionName)
            val respawnTime = this.plugin.configuration!!.getRegionRespawnTime(regionName)
            this.sendPlayerNormal(
                player,
                "$regionName : [W] $worldName [L] $left; [R] $right; [T]${respawnTime}s."
            )
        }
    }
}
