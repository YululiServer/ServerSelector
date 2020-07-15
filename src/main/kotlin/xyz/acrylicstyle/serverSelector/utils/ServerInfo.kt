package xyz.acrylicstyle.serverSelector.utils

import org.bukkit.Material
import util.CollectionList

class ServerInfo(
    val server: String,
    val name: String,
    val material: Material,
    val description: CollectionList<String>
)