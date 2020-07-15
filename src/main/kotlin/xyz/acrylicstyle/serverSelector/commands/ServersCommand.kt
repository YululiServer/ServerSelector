package xyz.acrylicstyle.serverSelector.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import xyz.acrylicstyle.serverSelector.ServerSelector
import xyz.acrylicstyle.tomeito_api.command.PlayerCommandExecutor
import xyz.acrylicstyle.tomeito_api.utils.TabCompleterHelper
import java.util.Collections

class ServersCommand : PlayerCommandExecutor(), TabCompleter {
    override fun onCommand(player: Player, args: Array<String>) {
        if (args.isNotEmpty() && player.isOp && args[0] == "reload") {
            ServerSelector.reload()
            player.sendMessage(ChatColor.GREEN.toString() + "Reloaded configuration.")
            return
        }
        player.openInventory(ServerSelector.gui.get(player.uniqueId).inv)
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): MutableList<String> {
        if (!sender.isOp) return Collections.emptyList()
        return TabCompleterHelper.filterArgsList(Collections.singletonList("reload"), if (args.isEmpty()) "" else args[0])
    }
}