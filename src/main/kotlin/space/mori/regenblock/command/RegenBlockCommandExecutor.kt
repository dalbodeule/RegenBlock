package space.mori.regenblock.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.*
import org.bukkit.*
import space.mori.regenblock.RegenBlock
import org.bukkit.entity.Player
import space.mori.regenblock.util.BlockList


class RegenBlockCommandExecutor(private val plugin: RegenBlock) : CommandBase() {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return if (sender !is Player) {
            plugin.log!!.info("/rb is only available in game.")
            true
        } else if (args.isEmpty() || args[0] == "help" || !SubCommands.keys.contains(args[0])) {
            plugin.log!!.sendPlayerNormal(sender,"RegenBlock commands")
            SubCommands.forEach {
                plugin.log!!.sendPlayerNormal(sender, "${command.name} ${it.value.name} ${it.value.parameter} - ${it.value.description}")
            }
            true
        } else {
            SubCommands[args[0]]!!.commandExecutor(sender, command, label, args)
        }
    }

    override val SubCommands: Map<String, SubCommand> = listOf(
        object: SubCommand (
            "monitor",
            "Monitor some place",
            "[break/place] [true/false]"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                if (args.size <= 2) {
                    plugin.log!!.sendPlayerNormal(
                        sender as Player,
                        "Monitor place - " + plugin.configuration!!.doMonitorPlace() + ", break - " + plugin.configuration!!.doMonitorBreak()
                    )
                } else if (args[1].equals("break", ignoreCase = true)) {
                    if (args[2].equals("false", ignoreCase = true)) {
                        plugin.configuration!!.setMonitorBreak(false)
                    } else {
                        plugin.configuration!!.setMonitorBreak(true)
                    }
                } else if (args[1].equals("place", ignoreCase = true)) {
                    if (args[2].equals("false", ignoreCase = true)) {
                        plugin.configuration!!.setMonitorPlace(false)
                    } else {
                        plugin.configuration!!.setMonitorPlace(true)
                    }
                } else {
                    plugin.log!!.sendPlayerNormal(
                        sender as Player,
                        "Monitor place - ${plugin.configuration!!.doMonitorPlace()}, break - ${plugin.configuration!!.doMonitorBreak()}"
                    )
                }

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                val arg = mutableListOf<String>()

                if (args.size <= 2) {
                    listOf("", "break", "place").forEach {
                        arg.add(it)
                    }
                } else if (args[1] == "place" || args[1] == "break") {
                    listOf("true", "false").forEach {
                        arg.add(it)
                    }
                }

                return arg
            }
        },
        object: SubCommand (
            "reload",
            "restores all blocks in queue and reloads the configuration file",
            ""
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String> ): Boolean {
                plugin.log!!.sendPlayerNormal(sender as Player, "RegenBlock is reloading settings.")
                plugin.configuration!!.reload()
                plugin.log!!.sendPlayerNormal(sender, "Done.")

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand (
            "blacklist",
            "lists, adds, removes blacklisted blocks for all regions.",
            "[add/remove] block block block..."
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player
                
                if (args.size < 2) {
                    plugin.log!!.sendPlayerNormal(
                        player,
                        StringBuilder().append(plugin.configuration!!.listBlacklistBlock()).toString()
                    )
                    return true
                }
                if (args.size < 3) {
                    printUse(player, RootCommand.blacklist)
                    return true
                }
                if (args[1].equals("add", ignoreCase = true)) {
                    for (i in 2 until args.size) {
                        val material = Material.getMaterial(args[i])

                        if (material != null) {
                            plugin.configuration!!.addBlacklistBlock(material)
                        }
                    }
                } else {
                    if (!args[1].equals("remove", ignoreCase = true)) {
                        printUse(player, RootCommand.blacklist)
                        return true
                    }
                    for (i in 2 until args.size) {
                        val material = Material.getMaterial(args[i])

                        if (material != null) {
                            plugin.configuration!!.removeBlacklistBlock(material)
                        }
                    }
                }
                plugin.log!!.sendPlayerNormal(
                    player,
                    "Blacklist updated. " + plugin.configuration!!.listBlacklistBlock()
                )

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size <= 2) {
                    listOf("", "add", "remove").forEach {
                        arg.add(it)
                    }
                } else if (args[1] == "remove") {
                    arg = plugin.configuration!!.listBlacklistBlock()?.toMutableList() ?: mutableListOf()
                } else if (args[1] == "add") {
                    arg = BlockList.toMutableList()
                }

                return arg
            }
        },
        object: SubCommand (
            "listselection",
            "Lists player's current selection",
            ""
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                return listselection(sender as Player)
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand (
            "edit",
            "Puts you into editor mode that allows you to change blocks in a region",
            ""
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (plugin.listenerPlayer!!.playerEditStatus.contains(player.name)) {
                    plugin.listenerPlayer!!.playerEditStatus.remove(player.name)
                    plugin.log!!.sendPlayerNormal(player, "Edit mode is OFF")
                } else {
                    plugin.listenerPlayer!!.playerEditStatus.add(player.name)
                    plugin.log!!.sendPlayerNormal(player, "Edit mode is ON")
                }

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand (
            "select",
            "Toggles selection mode for the player. If ex/ey/ez is specified selection will be expanded in that direction. Y is vertical.",
            "[ex,ey,ez]"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size == 1) {
                    if (plugin.listenerPlayer!!.playerSelectionStatus.contains(player.name)) {
                        plugin.listenerPlayer!!.playerSelectionStatus.remove(player.name)
                        plugin.log!!.sendPlayerNormal(player, "Selection mode is OFF")
                    } else {
                        plugin.listenerPlayer!!.playerSelectionStatus.add(player.name)
                        plugin.log!!.sendPlayerNormal(player, "Selection mode is ON")
                    }
                } else {
                    if (
                        !plugin.listenerPlayer!!.playerSelectionLeft.containsKey(player.name) ||
                        !plugin.listenerPlayer!!.playerSelectionRight.containsKey(player.name)
                    ) {
                        plugin.log!!.sendPlayerWarn(
                            player,
                            "Set both left and right points before modifying your selection through commands."
                        )
                        return true
                    }
                    if (args[1].equals("ex", ignoreCase = true)) {
                        plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!.x = -1000000.0
                        plugin.listenerPlayer!!.playerSelectionRight[player.name]!!.x = 1000000.0
                        listselection(sender)
                        return true
                    }
                    if (args[1].equals("ey", ignoreCase = true)) {
                        plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!.y = 0.0
                        plugin.listenerPlayer!!.playerSelectionRight[player.name]!!.y = 254.0
                        listselection(sender)
                        return true
                    }
                    if (args[1].equals("ez", ignoreCase = true)) {
                        plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!.z = -1000000.0
                        plugin.listenerPlayer!!.playerSelectionRight[player.name]!!.z = 1000000.0
                        listselection(sender)
                        return true
                    }
                    printUse(player, RootCommand.select)
                }

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                val arg = mutableListOf<String>()

                if (args.size <= 2) {
                    listOf("", "ex", "ey", "ez").forEach {
                        arg.add(it)
                    }
                }

                return arg
            }
        },
        object: SubCommand (
            "create",
            "creates a new region at points selected with optional re-spawn time, default otherwise.",
            "(name) [re-spawn time]"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (
                    !plugin.listenerPlayer!!.playerSelectionLeft.containsKey(player.name) ||
                    !plugin.listenerPlayer!!.playerSelectionRight.containsKey(player.name)
                ) {
                    plugin.log!!.sendPlayerWarn(player, "You need to select two points before creating a region.")
                    return true
                }
                if (args.size < 2) {
                    printUse(player, RootCommand.create)
                    return true
                }
                val regionName = args[1].toLowerCase()
                if (plugin.configuration!!.getRegionName(regionName) != null) {
                    plugin.log!!.sendPlayerWarn(player, "Region name is already in use.")
                    return true
                }
                var respawnTime = 1
                if (args.size == 3) {
                    try {
                        respawnTime = Integer.parseInt(args[2])
                    } catch (ex: NumberFormatException) {
                    }

                    if (respawnTime < 1) {
                        respawnTime = plugin.configuration!!.regionDefaultRespawnTime
                    }
                } else {
                    respawnTime = plugin.configuration!!.regionDefaultRespawnTime
                }
                plugin.configuration!!.setRegion(
                    regionName,
                    respawnTime,
                    plugin.listenerPlayer!!.playerSelectionRight[player.name]!!,
                    plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!
                )
                plugin.log!!.sendPlayerRegionInfo(player, regionName)

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand (
            "remove",
            "removes region from the list.",
            "remove (name)"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size == 1) {
                    printUse(player, RootCommand.remove)
                    return true
                }
                val regionName = args[1].toLowerCase()
                if (plugin.configuration!!.getRegionName(regionName) == null) {
                    plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                    return true
                }
                plugin.configuration!!.removeRegion(regionName)
                plugin.log!!.sendPlayerNormal(player, "Region $regionName was removed.")
                plugin.log!!.info("${player.name} removed region $regionName")

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if(args.size <= 2) {
                   arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                }

                return arg
            }
        },
        object: SubCommand (
            "modify",
            "",
            "(both/time) (name) [re-spawn time] modify existing region's location and time."
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 4) {
                    printUse(player, RootCommand.modify)
                    return true
                }
                when {
                    args[1].equals("time", ignoreCase = true) -> {
                        val regionName = args[2].toLowerCase()
                        if (plugin.configuration!!.getRegionName(regionName) == null) {
                            plugin.log!!.sendPlayerWarn(player, "Region name does not exist.")
                            return true
                        }
                        var respawnTime = 1
                        try {
                            respawnTime = Integer.parseInt(args[3])
                        } catch (ex: NumberFormatException) {
                        }

                        if (respawnTime < 1) {
                            respawnTime = plugin.configuration!!.regionDefaultRespawnTime
                        }
                        plugin.configuration!!.setRegionRespawnTime(regionName, respawnTime)
                        plugin.log!!.sendPlayerNormal(
                            player,
                            "Region $regionName was updated to respawn time of  ${respawnTime}s."
                        )
                        plugin.log!!.info("${player.name} updated region $regionName to respawn time of ${respawnTime}s.")
                    }
                    args[1].equals("both", ignoreCase = true) -> {
                        val regionName = args[2].toLowerCase()
                        if (plugin.configuration!!.getRegionName(regionName) == null) {
                            plugin.log!!.sendPlayerWarn(player, "Region name does not exist.")
                            return true
                        }
                        var respawnTime = 1
                        if (args.size == 4) {
                            try {
                                respawnTime = Integer.parseInt(args[3])
                            } catch (ex2: NumberFormatException) {
                            }

                            if (respawnTime < 1) {
                                respawnTime = plugin.configuration!!.getRegionRespawnTime(regionName)!!
                            }
                        } else {
                            respawnTime = plugin.configuration!!.getRegionRespawnTime(regionName)!!
                        }
                        plugin.configuration!!.setRegion(
                            regionName,
                            respawnTime,
                            plugin.listenerPlayer!!.playerSelectionRight[player.name]!!,
                            plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!
                        )
                        plugin.log!!.sendPlayerRegionInfo(player, regionName)

                        plugin.log!!.sendPlayerNormal(
                            player,
                            "Region $regionName was updated to area with respawn time of  ${respawnTime}s."
                        )
                        plugin.log!!.info("${player.name} updated region $regionName to area with respawn time of ${respawnTime}s.")
                    }
                    else -> {
                        printUse(player, RootCommand.modify)
                        return true
                    }
                }

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size <= 2) {
                    listOf("time", "both").forEach {
                        arg.add(it)
                    }
                } else if (args[1] == "time" || args[1] == "both") {
                    arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                }

                return arg
            }
        },
        object: SubCommand (
            "list",
            "lists current regions",
            ""
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (plugin.configuration!!.listRegions() != null) {
                    plugin.log!!.listRegion(player, plugin.configuration!!.listRegions()!!)
                } else {
                    plugin.log!!.sendPlayerNormal(player, "There are no regions.")
                }

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                return mutableListOf()
            }
        },
        object: SubCommand (
            "rblacklist",
            "lists, adds, removes blacklisted blocks for the region.",
            "(name) [add/remove] block block block..."
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 2) {
                    printUse(player, RootCommand.rblacklist)
                    return true
                }
                val regionName = args[1].toLowerCase()
                if (plugin.configuration!!.getRegionName(regionName) == null) {
                    plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                    return true
                }
                if (args.size == 2) {
                    plugin.log!!.sendPlayerNormal(
                        player,
                        StringBuilder().append(plugin.configuration!!.listRegionBlacklistBlock(regionName)).toString()
                    )
                    return true
                }
                if (args[2].equals("add", ignoreCase = true)) {
                    for (i in 3 until args.size) {
                        val material = Material.getMaterial(args[i])

                        if (material != null) {
                            plugin.configuration!!.addRegionBlacklistBlock(args[2], material)
                        }
                    }
                } else {
                    if (!args[2].equals("remove", ignoreCase = true)) {
                        printUse(player, RootCommand.rblacklist)
                        return true
                    }
                    for (i in 3 until args.size) {
                        val material = Material.getMaterial(args[i])

                        if (material != null) {
                            plugin.configuration!!.removeRegionBlacklistBlock(args[2], material)
                        }
                    }
                }
                plugin.log!!.sendPlayerNormal(player, "Region's blacklist updated.")

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                when {
                    args.size <= 2 -> arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                    args.size <= 3 -> listOf("add", "remove").forEach {
                        arg.add(it)
                    }
                    args[2] == "remove" -> arg = plugin.configuration!!.listRegionBlacklistBlock(args[1])?.toMutableList() ?: mutableListOf()
                    args[2] == "add" -> arg = BlockList.toMutableList()
                }

                return arg
            }
        },
        object: SubCommand (
            "type",
            "changes the region's type. 0 - normal, 1 - regen up only, with randomization based on spawnblocks.",
            "(name) (type - 0,1)"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 2) {
                    printUse(player, RootCommand.type)
                    return true
                }
                val regionName = args[1].toLowerCase()
                if (plugin.configuration!!.getRegionName(regionName) == null) {
                    plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                    return true
                }
                if (args.size < 3) {
                    plugin.log!!.sendPlayerNormal(
                        player,
                        "Region [$regionName] type is ${plugin.configuration!!.getRegionType(regionName)}"
                    )
                    return true
                }
                var type = 0
                try {
                    type = Integer.parseInt(args[2])
                } catch (ex: NumberFormatException) {
                }

                plugin.configuration!!.setRegionType(regionName, type)
                if (type == 1) {
                    plugin.configuration!!.regionAddSpawnBlocks(regionName)
                }
                plugin.log!!.sendPlayerNormal(player, "Region [$regionName] type was set to $type")

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size <= 2) {
                        arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                } else {
                    listOf("0", "1").forEach {
                        arg.add(it)
                    }
                }

                return arg
            }
        },
        object: SubCommand (
            "sync",
            "changes the region's sync state. 0 - blocks repop separately, 1 - all at once based on first block broken, 2 - all at once based on last block broken, 3 - Same as 2, but preserving the order.",
            "(name) (0/1/2/3)"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 2) {
                    printUse(player, RootCommand.sync)
                    return true
                }
                val regionName = args[1].toLowerCase()
                if (plugin.configuration!!.getRegionName(regionName) == null) {
                    plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                    return true
                }
                if (args.size < 3) {
                    plugin.log!!.sendPlayerNormal(
                        player,
                        "Region [$regionName] sync is ${plugin.configuration!!.getRegionSync(regionName)}"
                    )
                    return true
                }
                var sync = 0
                try {
                    sync = Integer.parseInt(args[2])
                } catch (ex: NumberFormatException) {
                }

                plugin.configuration!!.setRegionSync(regionName, sync)
                plugin.log!!.sendPlayerNormal(player, "Region [$regionName] sync was set to $sync")

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size <= 2) {
                    arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                } else {
                    listOf("0", "1", "2", "3").forEach {
                        arg.add(it)
                    }
                }

                return arg
            }
        },
        object: SubCommand (
            "alarm",
            "changes the region's alarm settings.",
            "(time/message/radius) (name) (value)"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 4) {
                    printUse(player, RootCommand.alarm)
                    return true
                }
                val regionName = args[2].toLowerCase()
                if (plugin.configuration!!.getRegionName(regionName) == null) {
                    plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                    return true
                }
                if (args[1].equals("time", ignoreCase = true)) {
                    var time = 0
                    try {
                        time = Integer.parseInt(args[3])
                    } catch (ex: NumberFormatException) {
                    }

                    plugin.configuration!!.setRegionAlarmTime(regionName, time)
                    plugin.log!!.sendPlayerNormal(player, "Alarm time of [$regionName] was set to $time.")
                }
                if (args[1].equals("message", ignoreCase = true)) {
                    var message = ""
                    for (i in 4 until args.size) {
                        message = message + args[i] + " "
                    }
                    plugin.configuration!!.setRegionAlarmMessage(regionName, message)
                    plugin.log!!.sendPlayerNormal(player, "Alarm message of [$regionName] was set to [$message].")
                }
                if (args[1].equals("radius", ignoreCase = true)) {
                    var radius = 0
                    try {
                        radius = Integer.parseInt(args[3])
                    } catch (ex2: NumberFormatException) {
                    }

                    plugin.configuration!!.setRegionAlarmRadius(regionName, radius)
                    plugin.log!!.sendPlayerNormal(player, "Alarm radius of [$regionName] was set to $radius.")
                }

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

            if (args.size <= 2) {
                    listOf("time", "message", "radius").forEach {
                        arg.add(it)
                    }
            } else if (args[1] == "time" || args[1] == "message" || args[1] == "radius") {
                if(args.size <= 3) {
                    arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                }
            }

                return arg
            }
        },
        object: SubCommand (
            "feedback",
            "region: changes the region's feedback type, set: sets string sent to player during region feedback. Use TIME to show re-spawn time.",
            "(region (name) (feedback type [0,1,2])/set (string))"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 3) {
                    printUse(player, RootCommand.feedback)
                    return true
                }
                when {
                    args[1].equals("set", ignoreCase = true) -> {
                        var feedbackString = ""
                        for (i in 3 until args.size) {
                            feedbackString = feedbackString + args[i] + " "
                        }
                        if (feedbackString.isNotEmpty()) {
                            plugin.configuration!!.feedbackString = feedbackString
                            plugin.log!!.sendPlayerNormal(player, "Feedback string was set to [$feedbackString]")
                        } else {
                            plugin.log!!.sendPlayerWarn(player, "Feedback string was not changed.")
                        }
                        return true
                    }
                    args[1].equals("region", ignoreCase = true) -> {
                        val regionName = args[2].toLowerCase()
                        if (plugin.configuration!!.getRegionName(regionName) == null) {
                            plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                            return true
                        }
                        var feedbackId = 0
                        try {
                            feedbackId = Integer.parseInt(args[3])
                        } catch (ex: NumberFormatException) {
                        }

                        feedbackId = plugin.configuration!!.setRegionFeedbackID(regionName, feedbackId)
                        plugin.log!!.sendPlayerNormal(player, "Region $regionName feedback type was set to $feedbackId")

                        return true
                    }
                    else -> {
                        printUse(player, RootCommand.feedback)
                        return true
                    }
                }
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size <= 2) {
                    listOf("set", "region").forEach {
                        arg.add(it)
                    }
                } else if(args[1] == "region") {
                    if (args.size <= 3) {
                        arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                    } else {
                        listOf("0", "1", "2").forEach {
                            arg.add(it)
                        }
                    }
                }

                return arg
            }
        },
        object: SubCommand (
            "spawnblock",
            "list: lists region's spawn blocks, add: adds new blocks with spawn chance, remove: removes blocks.",
            "(list/add [block chance block chance...]/remove [block block block...])"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 3) {
                    printUse(player, RootCommand.spawnblock)
                    return true
                }
                when {
                    args[1].equals("add", ignoreCase = true) -> {
                        val regionName = args[2].toLowerCase()
                        if (plugin.configuration!!.getRegionName(regionName) == null) {
                            plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                            return true
                        }
                        if (args.size < 5) {
                            printUse(player, RootCommand.spawnblock)
                            return true
                        }
                        var i = 3
                        while (i + 1 < args.size) {
                            val material = Material.getMaterial(args[i])

                            var chance = 0
                            try {
                                chance = Integer.parseInt(args[i + 1])
                            } catch (ex2: NumberFormatException) {
                            }

                            if (material != null && chance > 0) {
                                plugin.configuration!!.setRegionSpawnBlock(regionName, material, chance)
                            }
                            i += 2
                        }
                        plugin.log!!.sendPlayerNormal(
                            player,
                            "Region spawn blocks: " + plugin.configuration!!.getRegionSpawnBlocks(regionName)!!
                        )
                    }
                    args[1].equals("remove", ignoreCase = true) -> {
                        val regionName = args[2].toLowerCase()
                        if (plugin.configuration!!.getRegionName(regionName) == null) {
                            plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                            return true
                        }
                        if (args.size < 4) {
                            printUse(player, RootCommand.spawnblock)
                            return true
                        }
                        for (i in 3 until args.size) {
                            val material = Material.getMaterial(args[i])

                            if (material != null) {
                                plugin.configuration!!.removeRegionSpawnBlock(regionName, material)
                            }
                        }
                        plugin.log!!.sendPlayerNormal(
                            player,
                            "Region spawn blocks: " + plugin.configuration!!.getRegionSpawnBlocks(regionName)!!
                        )
                    }
                    args[1].equals("list", ignoreCase = true) -> {
                        val regionName = args[2].toLowerCase()
                        if (plugin.configuration!!.getRegionName(regionName) == null) {
                            plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                            return true
                        }
                        plugin.log!!.sendPlayerNormal(
                            player,
                            "Region spawn blocks: " + plugin.configuration!!.getRegionSpawnBlocks(regionName)!!
                        )
                    }
                    else -> {
                        printUse(player, RootCommand.spawnblock)
                        return true
                    }
                }

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size <= 2) {
                    listOf("add", "remove", "list").forEach {
                        arg.add(it)
                    }
                } else if (args[1] == "remove") {
                    if (args.size <= 3) {
                        arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                    } else {
                        arg = plugin.configuration!!.getRegionSpawnBlocks(args[2])!!.keys.map { it.name }.toMutableList()
                    }
                } else if (args[1] == "add") {
                    if (args.size <= 3) {
                        arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                    } else if (args.size%2 == 0) {
                        arg = BlockList.toMutableList()
                    }
                } else if (args[1] == "list") {
                    arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                }

                return arg
            }
        },
        object: SubCommand (
            "info",
            "Print out information about the region in front of the character",
            ""
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                val block = player.getTargetBlock(null as HashSet<Material>?, 100)
                if (block == null) {
                    plugin.log!!.sendPlayerWarn(player, "No region targeted.")
                    return true
                }
                val regionName = plugin.listenerBlock!!.getBlockRegion(block.location)
                if (regionName == null) {
                    plugin.log!!.sendPlayerWarn(player, "No region targeted.")
                    return true
                }
                plugin.log!!.sendPlayerRegionInfo(player, regionName)

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size < 2) {
                    arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                }

                return arg
            }
        },
        object: SubCommand (
            "repop",
            "Respawns all blocks in a given region.",
            "(name)"
        ) {
            override fun commandExecutor(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                val player = sender as Player

                if (args.size < 2) {
                    printUse(player, RootCommand.repop)
                    return true
                }
                val regionName = args[1].toLowerCase()
                if (plugin.configuration!!.getRegionName(regionName) == null) {
                    plugin.log!!.sendPlayerWarn(player, "Region $regionName does not exist.")
                    return true
                }
                plugin.queue!!.regenRegion(regionName)

                return true
            }

            override fun tabCompleter(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
                var arg = mutableListOf<String>()

                if (args.size <= 2) {
                    arg = plugin.configuration!!.listRegions()?.toMutableList() ?: mutableListOf()
                }

                return arg
            }
        }
    ).associateBy { it.name }.toSortedMap()

    private fun listselection(player: Player): Boolean {
        var right = "Nothing is selected"
        var left = "Nothing is selected"
        if (plugin.listenerPlayer!!.playerSelectionLeft.containsKey(player.name)) {
            left =
                "${plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!.blockX} ${plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!.blockY} " +
                    "${plugin.listenerPlayer!!.playerSelectionLeft[player.name]!!.blockZ}"
        }
        if (plugin.listenerPlayer!!.playerSelectionRight.containsKey(player.name)) {
            right =
                "${plugin.listenerPlayer!!.playerSelectionRight[player.name]!!.blockX} ${plugin.listenerPlayer!!.playerSelectionRight[player.name]!!.blockY} " +
                    "${plugin.listenerPlayer!!.playerSelectionRight[player.name]!!.blockZ}"
        }
        plugin.log!!.sendPlayerNormal(player, "Right: $right Left: $left")

        return true
    }

    private fun printUse(player: Player, command: RootCommand) {
        when (command) {
            RootCommand.blacklist -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb blacklist [add/remove] [block] - lists, adds, removes blacklisted block IDs for all regions."
                )
            }
            RootCommand.create -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb create (name) [re-spawn time] - creates a new region at points selected with optional re-spawn time, default otherwise."
                )
            }
            RootCommand.remove -> {
                plugin.log!!.sendPlayerWarn(player, "Usage: /rb remove (name) - removes region from the list.")
            }
            RootCommand.modify -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb modify (name) [re-spawn time] - modify existing region's location and time."
                )
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb modify time (name) (re-spawn time) - modify existing region's re-spawn time only."
                )
            }
            RootCommand.rblacklist -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb rblacklist (name) [add/remove] [block] - lists, adds, removes blacklisted block IDs for the region."
                )
            }
            RootCommand.type -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb type (name) (type - 0,1)- changes the region's type. 0 - normal, 1 - regen up only, with randomization based on spawnblocks"
                )
            }
            RootCommand.sync -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb sync (name) (0/1/2/3)- changes the region's sync state. 0 - blocks repop separately, 1 - all at once based on first block broken, 2 - all at once based on last block broken, 3 - Same as 2, but preserving the order."
                )
            }
            RootCommand.alarm -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb alarm time/message/radius (name) (value)- changes the region's alarm settings."
                )
            }
            RootCommand.feedback -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb feedback (name) (feedback type [0,1,2])- changes the region's feedback type."
                )
            }
            RootCommand.repop -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb repop (name) - Respawns all blocks in a given region."
                )
            }
            RootCommand.spawnblock -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb spawnblock list (name) - lists region's spawn blocks.")
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb spawnblock add (name) [block chance block chance...] - adds new blocks with spawn chance."
                )
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb spawnblock remove (name) [block block block...] - removes blocks."
                )
            }
            RootCommand.select -> {
                plugin.log!!.sendPlayerWarn(
                    player,
                    "Usage: /rb select [ex,ey,ez] - Toggles selection mode for the player. If ex/ey/ez is specified selection will be expanded in that direction. Y is vertical."
                )
            }
        }
    }

    private enum class RootCommand {
        blacklist,
        select,
        create,
        remove,
        modify,
        rblacklist,
        type,
        sync,
        alarm,
        feedback,
        spawnblock,
        repop
    }
}
