package xyz.acrylicstyle.serverSelector.listener

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import util.ICollectionList
import xyz.acrylicstyle.serverSelector.ServerSelector
import xyz.acrylicstyle.tomeito_api.utils.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class BungeeCordChannelListener : PluginMessageListener {
    @ExperimentalStdlibApi
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel == "BungeeCord") {
            val input = DataInputStream(ByteArrayInputStream(message))
            val s = input.readUTF()
            if (s == "PlayerCount") {
                val server = input.readUTF()
                val players = input.readInt()
                if (ServerSelector.debug) Log.info("BungeeCord -> PlayerCount -> server=$server, players=$players")
                ServerSelector.playerCount[server] = players
            } else if (s == "GetServers") {
                val servers = input.readUTF().split(", ")
                if (ServerSelector.debug) Log.info("BungeeCord -> GetServers -> " + ICollectionList.asList(servers).join(", "))
                servers.forEach { server ->
                    checkPlayerCount(server)
                }
            }
        }
    }

    @ExperimentalStdlibApi
    private fun checkPlayerCount(server: String) {
        val player = Bukkit.getOnlinePlayers().randomOrNull() ?: return
        val stream = ByteArrayOutputStream()
        val output = DataOutputStream(stream)
        output.writeUTF("PlayerCount")
        output.writeUTF(server)
        if (ServerSelector.debug) Log.info("BungeeCord <- PlayerCount <- server=$server")
        output.close()
        player.sendPluginMessage(ServerSelector.instance, "BungeeCord", stream.toByteArray())
        stream.close()
    }
}
