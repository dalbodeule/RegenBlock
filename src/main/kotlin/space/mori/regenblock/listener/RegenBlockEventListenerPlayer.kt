package space.mori.regenblock.listener

import space.mori.regenblock.*
import org.bukkit.*
import java.util.*
import org.bukkit.event.block.*
import org.bukkit.event.*
import org.bukkit.event.player.*

class RegenBlockEventListenerPlayer(private val plugin: RegenBlock) : Listener {
    val playerSelectionLeft: HashMap<String, Location> = HashMap()
    val playerSelectionRight: HashMap<String, Location> = HashMap()
    val playerSelectionStatus: ArrayList<String> = ArrayList()
    val playerEditStatus: ArrayList<String> = ArrayList()

    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if (event.isCancelled) {
            return
        }
        if (!this.playerSelectionStatus.contains(event.player.name)) {
            return
        }
        val player = event.player
        val loc = event.clickedBlock.location
        val action = event.action
        if (action == Action.LEFT_CLICK_BLOCK) {
            val locOld = this.playerSelectionLeft[player.name]
            if (locOld != null && locOld != loc as Any || locOld == null) {
                this.playerSelectionLeft[player.name] = loc
                this.plugin.log!!.sendPlayerNormal(
                    player,
                    "Left Block: ${loc.blockX} ${loc.blockY} ${loc.blockZ}"
                )
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            val locOld = this.playerSelectionRight[player.name]
            if (locOld != null && locOld != loc as Any || locOld == null) {
                this.playerSelectionRight[player.name] = loc
                this.plugin.log!!.sendPlayerNormal(
                    player,
                    "Right Block: ${loc.blockX} ${loc.blockY} ${loc.blockZ}"
                )
            }
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onEvent(event: PlayerChangedWorldEvent) {
        val player = event.player
        this.playerSelectionLeft.remove(player.name)
        this.playerSelectionRight.remove(player.name)
        this.playerSelectionStatus.remove(player.name)
        this.playerEditStatus.remove(player.name)
    }

    @EventHandler
    fun onEvent(event: PlayerJoinEvent) {
        val player = event.player
        this.playerSelectionLeft.remove(player.name)
        this.playerSelectionRight.remove(player.name)
        this.playerSelectionStatus.remove(player.name)
        this.playerEditStatus.remove(player.name)
    }

    @EventHandler
    fun onEvent(event: PlayerQuitEvent) {
        val player = event.player
        this.playerSelectionLeft.remove(player.name)
        this.playerSelectionRight.remove(player.name)
        this.playerSelectionStatus.remove(player.name)
        this.playerEditStatus.remove(player.name)
    }
}
