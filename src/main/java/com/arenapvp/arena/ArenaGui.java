package com.arenapvp.arena;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.manager.ArenaManager;
import com.arenapvp.message.MessageService;
import com.arenapvp.model.ArenaLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class ArenaGui implements Listener {

    private static final NamespacedKey ARENA_KEY = new NamespacedKey("arenapvp", "arena");

    private final ArenaPVPPlugin plugin;
    private final ArenaManager arenaManager;
    private final Inventory inventory;

    public ArenaGui(ArenaPVPPlugin plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        int rows = Math.min(6, Math.max(1, plugin.pluginConfig().getArenaGuiRows()));
        Component title = MessageService.LEGACY.deserialize(
                MessageService.color(plugin.pluginConfig().getArenaGuiTitle()));
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        populate();
    }

    private void populate() {
        List<String> names = arenaManager.arenaNames();
        for (int i = 0; i < names.size() && i < inventory.getSize(); i++) {
            String name = names.get(i);
            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MessageService.LEGACY.deserialize(MessageService.color("&e" + name)));
            meta.lore(List.of(
                    MessageService.LEGACY.deserialize(MessageService.color("&7Click to warp to this arena"))
            ));
            meta.getPersistentDataContainer().set(ARENA_KEY, PersistentDataType.STRING, name);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
    }

    public void open(Player player) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
        plugin.messages().send(player, "arena.gui-opened");
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }
        String arenaName = clicked.getItemMeta().getPersistentDataContainer().get(ARENA_KEY, PersistentDataType.STRING);
        if (arenaName == null) {
            return;
        }
        ArenaLocation arena = arenaManager.getArena(arenaName);
        if (arena != null) {
            arenaManager.teleport(player, arena);
        }
        player.closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
