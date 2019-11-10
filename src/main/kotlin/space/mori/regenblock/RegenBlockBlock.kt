package space.mori.regenblock

import org.bukkit.*
import org.bukkit.block.data.BlockData

class RegenBlockBlock(
    var location: Location?,
    type: Material,
    var data: BlockData?,
    var regionName: String?,
    var respawnTime: Long,
    var alarmTime: Int,
    var sync: Int,
    var alarmRadius: Int,
    var alarmMessage: String?,
    var regionType: Int
) : Comparable<RegenBlockBlock> {
    var type: Material? = null
        private set

    fun setTypeId(type: Material) {
        this.type = type
    }

    init {
        this.type = type
    }

    override fun compareTo(other: RegenBlockBlock): Int {
        if (this.respawnTime > other.respawnTime) {
            return -1
        }
        if (this.respawnTime == other.respawnTime && this.location!!.blockX > other.location!!.blockX) {
            return -1
        }
        if (this.respawnTime == other.respawnTime && this.location!!.blockZ > other.location!!.blockZ) {
            return -1
        }
        return if (this.respawnTime == other.respawnTime && this.location!!.blockY > other.location!!.blockY) {
            -1
        } else 1
    }
}
