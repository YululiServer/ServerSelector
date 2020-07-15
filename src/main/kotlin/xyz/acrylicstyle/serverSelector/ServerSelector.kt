package xyz.acrylicstyle.serverSelector

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import util.Collection
import util.ICollectionList
import xyz.acrylicstyle.serverSelector.commands.ServersCommand
import xyz.acrylicstyle.serverSelector.gui.ServerGui
import xyz.acrylicstyle.serverSelector.utils.ServerInfo
import xyz.acrylicstyle.tomeito_api.TomeitoAPI
import xyz.acrylicstyle.tomeito_api.gui.PerPlayerInventory
import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider
import xyz.acrylicstyle.tomeito_api.utils.Log
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class ServerSelector : JavaPlugin() {
    companion object {
        val servers: Collection<Int, ServerInfo> = Collection()

        lateinit var instance: ServerSelector

        val gui = PerPlayerInventory<ServerGui> { uuid -> ServerGui(uuid) }

        private fun getConfig(): ConfigProvider = instance.config

        fun transferPlayer(player: Player, server: String) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val o = DataOutputStream(byteArrayOutputStream)
            o.writeUTF("Connect")
            o.writeUTF(server)
            player.sendPluginMessage(instance, "BungeeCord", byteArrayOutputStream.toByteArray())
            o.close()
            byteArrayOutputStream.close()
        }

        fun reload() {
            getConfig().reload()
            servers.clear()
            for (i in 0 until 54) {
                try {
                    val map = getConfig().getConfigSectionValue("servers.slot$i", true) ?: continue
                    if (!map.containsKey("server")
                        || !map.containsKey("name")
                        || !map.containsKey("material")
                        || !map.containsKey("description")) continue
                    val server = (map["server"] ?: continue) as String
                    val name = (map["name"] ?: continue) as String
                    val material = map.getOrDefault("material", "STONE") as String
                    @Suppress("UNCHECKED_CAST") val description = map["description"] as List<String>
                    servers.add(i,
                        ServerInfo(
                            server,
                            name,
                            Material.getMaterial(material.toUpperCase()),
                            ICollectionList.asList(description).map { s -> ChatColor.translateAlternateColorCodes('&', s) }
                        )
                    )
                } catch (e: RuntimeException) {
                    Log.warn("Could not load server data from slot$i:")
                    e.printStackTrace()
                }
            }
        }
    }

    private lateinit var config: ConfigProvider

    init {
        instance = this
    }

    override fun onEnable() {
        config = ConfigProvider.getConfig("./plugins/ServerSelector/config.yml")
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord")
        val cmd = ServersCommand()
        TomeitoAPI.registerCommand("servers", cmd)
        TomeitoAPI.registerTabCompleter("servers", cmd)
        reload()
    }
}