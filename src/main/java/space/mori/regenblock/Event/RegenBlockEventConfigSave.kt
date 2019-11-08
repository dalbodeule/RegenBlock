package space.mori.regenblock.event

import org.bukkit.event.*

class RegenBlockEventConfigSave : Event() {

    override fun getHandlers(): HandlerList {
        return RegenBlockEventConfigSave.handlerList
    }

    companion object {
        @JvmStatic val handlerList: HandlerList = HandlerList()

    }
}
