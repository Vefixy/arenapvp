package com.arenapvp.kit;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.message.MessageService;
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

public final class KitGui implements Listener {

    private static final NamespacedKey KIT_KEY = new NamespacedKey("arenapvp", "kit");

    private final ArenaPVPPlugin plugin;
    private final KitManager kitManager;
    private final Inventory inventory;

    public KitGui(ArenaPVPPlugin plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        int rows = Math.min(6, Math.max(1, plugin.pluginConfig().getKitGuiRows()));
        Component title = MessageService.LEGACY.deserialize(
                MessageService.color(plugin.pluginConfig().getKitGuiTitle()));
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        populate();
    }

    private void populate() {
        List<String> kits = kitManager.availableKitNames();
        for (int i = 0; i < kits.size() && i < inventory.getSize(); i++) {
            String kit = kits.get(i);
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MessageService.LEGACY.deserialize(MessageService.color("&e" + kit)));
            meta.lore(List.of(
                    MessageService.LEGACY.deserialize(MessageService.color("&7Click to receive this kit"))
            ));
            meta.getPersistentDataContainer().set(KIT_KEY, PersistentDataType.STRING, kit);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
    }

    public void open(Player player) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
        plugin.messages().send(player, "kits.gui-opened");
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
        String kitName = clicked.getItemMeta().getPersistentDataContainer().get(KIT_KEY, PersistentDataType.STRING);
        if (kitName == null) {
            return;
        }
        kitManager.giveKit(player, kitName);
        player.closeInventory();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
