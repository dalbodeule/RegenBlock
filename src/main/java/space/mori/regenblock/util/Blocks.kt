package space.mori.regenblock.util

import org.bukkit.Material

val BlockList: List<String> = Material.values().filter {
    it.isBlock && !it.name.startsWith("LEGACY_")
}.map { it.name }