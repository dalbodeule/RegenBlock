package space.mori.regenblock.event

import org.bukkit.event.*

class RegenBlockEventAlarm(
    val alarmRadius: Int,
    val alarmMessage: String,
    val worldName: String,
    val x: Int,
    val y: Int,
    val z: Int
) : Event() {

    override fun getHandlers(): HandlerList {
        return RegenBlockEventAlarm.handlerList
    }

    companion object {
        @JvmStatic val handlerList: HandlerList = HandlerList()

    }
}
