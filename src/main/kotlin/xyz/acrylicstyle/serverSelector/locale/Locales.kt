package xyz.acrylicstyle.serverSelector.locale

import org.bukkit.ChatColor

interface Locales {
    companion object {
        private val DEFAULT_LOCALE = Locale_ja_JP // change me if you want to use other locales

        fun getLocale(locale: String): Locales {
            return when (locale.toLowerCase()) {
                "ja_jp" -> Locale_ja_JP
                else -> DEFAULT_LOCALE
            }
        }
    }

    fun clickToConnect(): String = ChatColor.GREEN.toString() + "> Click to connect"

    fun onlinePlayers(): String = ChatColor.GRAY.toString() + "Currently %s players online."
}