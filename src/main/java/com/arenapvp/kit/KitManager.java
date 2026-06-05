package com.arenapvp.kit;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.storage.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class KitManager {

    private final ArenaPVPPlugin plugin;
    private final DatabaseManager database;
    private final Map<String, LocalKit> localKits = new LinkedHashMap<>();

    public KitManager(ArenaPVPPlugin plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
        reload();
    }

    public void reload() {
        localKits.clear();
        try (PreparedStatement ps = database.connection().prepareStatement("SELECT name, contents, armor, offhand FROM local_kits");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                ItemStack[] contents = KitSerializer.deserialize(rs.getString("contents"), 36);
                ItemStack[] armor = KitSerializer.deserialize(rs.getString("armor"), 4);
                ItemStack offhand = rs.getString("offhand") == null
                        ? null
                        : KitSerializer.deserialize(rs.getString("offhand"), 1)[0];
                localKits.put(normalize(name), new LocalKit(name, contents, armor, offhand));
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to load local kits", ex);
        }
    }

    public List<String> availableKitNames() {
        List<String> kits = new ArrayList<>();
        String mode = plugin.pluginConfig().getKitsMode().toLowerCase(Locale.ROOT);
        if (mode.equals("local") || mode.equals("both")) {
            localKits.values().forEach(kit -> kits.add(kit.name()));
        }
        if (mode.equals("essentials") || mode.equals("both")) {
            kits.addAll(plugin.essentials().listKits());
        }
        return kits;
    }

    public List<String> localKitNames() {
        return localKits.values().stream().map(LocalKit::name).toList();
    }

    public boolean giveKit(Player player, String kitName) {
        String normalized = normalize(kitName);
        String mode = plugin.pluginConfig().getKitsMode().toLowerCase(Locale.ROOT);

        if ((mode.equals("local") || mode.equals("both")) && localKits.containsKey(normalized)) {
            applyLocalKit(player, localKits.get(normalized));
            plugin.messages().send(player, "kits.kit-received", Map.of("kit", localKits.get(normalized).name()));
            return true;
        }

        if ((mode.equals("essentials") || mode.equals("both")) && plugin.essentials().isAvailable()) {
            if (plugin.essentials().giveKit(player, kitName)) {
                plugin.messages().send(player, "kits.kit-received", Map.of("kit", kitName));
                return true;
            }
        }

        plugin.messages().send(player, "kits.kit-not-found", Map.of("kit", kitName));
        return false;
    }

    private void applyLocalKit(Player player, LocalKit kit) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setContents(copy(kit.contents()));
        inv.setArmorContents(copy(kit.armor()));
        if (kit.offhand() != null) {
            inv.setItemInOffHand(kit.offhand().clone());
        }
        player.updateInventory();
    }

    private static ItemStack[] copy(ItemStack[] source) {
        ItemStack[] copy = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] == null ? new ItemStack(Material.AIR) : source[i].clone();
        }
        return copy;
    }

    public boolean createLocalKit(Player player, String name) {
        String key = normalize(name);
        PlayerInventory inv = player.getInventory();
        LocalKit kit = new LocalKit(
                name,
                inv.getContents().clone(),
                inv.getArmorContents().clone(),
                inv.getItemInOffHand().clone()
        );
        try (PreparedStatement ps = database.connection().prepareStatement("""
                INSERT INTO local_kits (name, contents, armor, offhand)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(name) DO UPDATE SET
                    contents = excluded.contents,
                    armor = excluded.armor,
                    offhand = excluded.offhand
                """)) {
            ps.setString(1, key);
            ps.setString(2, KitSerializer.serialize(kit.contents()));
            ps.setString(3, KitSerializer.serialize(kit.armor()));
            ps.setString(4, kit.offhand() == null ? null : KitSerializer.serialize(new ItemStack[]{kit.offhand()}));
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save local kit " + name, ex);
            return false;
        }
        localKits.put(key, kit);
        plugin.messages().send(player, "kits.kit-created", Map.of("kit", name));
        return true;
    }

    public boolean deleteLocalKit(Player player, String name) {
        String normalized = normalize(name);
        if (!localKits.containsKey(normalized)) {
            plugin.messages().send(player, "kits.kit-not-found", Map.of("kit", name));
            return false;
        }
        String displayName = localKits.get(normalized).name();
        try (PreparedStatement ps = database.connection().prepareStatement("DELETE FROM local_kits WHERE name = ?")) {
            ps.setString(1, normalized);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete local kit " + name, ex);
            return false;
        }
        localKits.remove(normalized);
        plugin.messages().send(player, "kits.kit-deleted", Map.of("kit", displayName));
        return true;
    }

    public void openGui(Player player) {
        if (!plugin.pluginConfig().isKitsEnabled()) {
            plugin.messages().send(player, "kits.disabled");
            return;
        }
        new KitGui(plugin, this).open(player);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
