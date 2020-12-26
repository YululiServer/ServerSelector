package xyz.acrylicstyle.serverSelector.utils

import org.bukkit.Material
import util.ICollectionList

class ServerInfo(
    val server: String,
    val name: String,
    val material: Material,
    val description: ICollectionList<String>
)