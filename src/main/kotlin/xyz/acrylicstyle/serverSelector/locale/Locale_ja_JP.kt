package xyz.acrylicstyle.serverSelector.locale

import org.bukkit.ChatColor

object Locale_ja_JP: Locales {
    override fun clickToConnect(): String = ChatColor.GREEN.toString() + "> クリックしてサーバーに接続"

    override fun onlinePlayers(): String = ChatColor.GRAY.toString() + "現在%s人のプレイヤーがオンラインです。"
}