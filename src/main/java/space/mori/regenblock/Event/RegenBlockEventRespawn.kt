package space.mori.regenblock.event

import org.bukkit.event.*
import java.util.*
import space.mori.regenblock.*

class RegenBlockEventRespawn(val blocks: ArrayList<RegenBlockBlock>) : Event() {

    override fun getHandlers(): HandlerList {
        return RegenBlockEventRespawn.handlerList
    }

    companion object {
        @JvmStatic val handlerList: HandlerList = HandlerList()

    }
}
