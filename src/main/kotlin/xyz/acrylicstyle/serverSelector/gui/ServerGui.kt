package xyz.acrylicstyle.serverSelector.gui

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import xyz.acrylicstyle.serverSelector.ServerSelector
import xyz.acrylicstyle.tomeito_api.utils.ReflectionUtil
import java.util.UUID

class ServerGui(uuid: UUID) : InventoryHolder, Listener {
    private val locale: String

    init {
        Bukkit.getPlayer(uuid)
        val handle = ReflectionUtil.getOBCClass("entity.CraftPlayer").getDeclaredMethod("getHandle").invoke(Bukkit.getPlayer(uuid))
        locale = ReflectionUtil.getNMSClass("EntityPlayer").getDeclaredField("locale").get(handle) as String
        Bukkit.getPluginManager().registerEvents(this, ServerSelector.instance)
        setItems()
    }

    lateinit var inv: Inventory

    private fun setItems() {
        inv = Bukkit.createInventory(this, 54, "Server Selector")
        ServerSelector.servers.forEach { index, server ->
            if (index == null || server == null) return@forEach
            val item = ItemStack(server.material)
            val meta = item.itemMeta
            meta.displayName = ChatColor.translateAlternateColorCodes('&', server.name)
            val lore = server.description
            lore.add("")
            lore.add(if (locale.toLowerCase() == "ja_jp") ChatColor.GREEN.toString() + "> クリックして接続" else ChatColor.GREEN.toString() + "> Click to connect")
            meta.lore = server.description
            item.itemMeta = meta
            inv.setItem(index, item)
        }
    }

    override fun getInventory(): Inventory = inv

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.inventory.holder != this) return
        if (e.clickedInventory == null || e.clickedInventory.holder != this) return
        e.isCancelled = true
        val server = ServerSelector.servers[e.slot] ?: return
        if (server.server == "CLOSE") {
            e.whoClicked.closeInventory()
            return
        }
        ServerSelector.transferPlayer(e.whoClicked as Player, server.server)
    }
}