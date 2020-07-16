package xyz.acrylicstyle.serverSelector

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import util.Collection
import util.ICollectionList
import util.StringCollection
import xyz.acrylicstyle.serverSelector.commands.ServersCommand
import xyz.acrylicstyle.serverSelector.gui.ServerGui
import xyz.acrylicstyle.serverSelector.listener.BungeeCordChannelListener
import xyz.acrylicstyle.serverSelector.utils.ServerInfo
import xyz.acrylicstyle.tomeito_api.TomeitoAPI
import xyz.acrylicstyle.tomeito_api.gui.PerPlayerInventory
import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider
import xyz.acrylicstyle.tomeito_api.utils.Log
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class ServerSelector : JavaPlugin(), Listener {
    companion object {
        @JvmStatic
        val servers: Collection<Int, ServerInfo> = Collection()

        @JvmStatic
        lateinit var instance: ServerSelector

        @JvmStatic
        val gui = PerPlayerInventory<ServerGui> { uuid -> ServerGui(uuid) }

        val guiServers = PerPlayerInventory<Collection<Int, ServerInfo>> { _ -> Collection() }

        @JvmStatic
        val playerCount = StringCollection<Int>()

        @JvmStatic
        val checkRateLimit = AtomicBoolean()

        const val debug = false

        private fun getConfig(): ConfigProvider = instance.config

        fun transferPlayer(player: Player, server: String) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val o = DataOutputStream(byteArrayOutputStream)
            o.writeUTF("Connect")
            o.writeUTF(server)
            if (debug) Log.info("BungeeCord <- Connect <- player=${player.name}, server=$server")
            player.sendPluginMessage(instance, "BungeeCord", byteArrayOutputStream.toByteArray())
            o.close()
            byteArrayOutputStream.close()
        }

        fun reload() {
            gui.clear()
            getConfig().reload()
            servers.clear()
            for (i in 0 until 54) {
                try {
                    val map = getConfig().getConfigSectionValue("servers.slot$i", true) ?: continue
                    if (!map.containsKey("server")
                        || !map.containsKey("name")
                        || !map.containsKey("material")
                        || !map.containsKey("description")) {
                        //Log.warn("server, name, material, or description is missing @$i")
                        //Log.warn("Server: ${map["server"]}, Name: ${map["name"]}, Material: ${map["material"]}, Description: ${map["description"]}")
                        continue
                    }
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

    @ExperimentalStdlibApi
    override fun onEnable() {
        config = ConfigProvider.getConfig("./plugins/ServerSelector/config.yml")
        Bukkit.getPluginManager().registerEvents(this, this)
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord")
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", BungeeCordChannelListener())
        val cmd = ServersCommand()
        TomeitoAPI.registerCommand("servers", cmd)
        TomeitoAPI.registerTabCompleter("servers", cmd)
        reload()
        object: BukkitRunnable() {
            override fun run() {
                checkPlayerCounts()
            }
        }.runTaskTimer(this, 0, 100)
    }

    @ExperimentalStdlibApi
    fun checkPlayerCounts() {
        val player = Bukkit.getOnlinePlayers().randomOrNull() ?: return
        val stream = ByteArrayOutputStream()
        val output = DataOutputStream(stream)
        output.writeUTF("GetServers")
        if (debug) Log.info("BungeeCord <- GetServers")
        output.close()
        player.sendPluginMessage(this, "BungeeCord", stream.toByteArray())
        stream.close()
    }

    @ExperimentalStdlibApi
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (!checkRateLimit.get()) {
            checkPlayerCounts()
            checkRateLimit.set(true)
            object: BukkitRunnable() {
                override fun run() {
                    checkRateLimit.set(false)
                }
            }.runTaskLater(this, 50)
        }
    }
}