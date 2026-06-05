package com.arenapvp.manager;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.model.ArenaLocation;
import com.arenapvp.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class ArenaManager {

    private final ArenaPVPPlugin plugin;
    private final DatabaseManager database;
    private final Map<String, ArenaLocation> arenas = new LinkedHashMap<>();

    public ArenaManager(ArenaPVPPlugin plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
        reload();
    }

    public void reload() {
        arenas.clear();
        try (PreparedStatement ps = database.connection().prepareStatement(
                "SELECT name, world, x, y, z, yaw, pitch FROM arenas");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                arenas.put(normalize(name), new ArenaLocation(
                        name,
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")
                ));
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to load arenas", ex);
        }
    }

    public void setArena(Player player, String name) {
        Location loc = player.getLocation();
        String key = normalize(name);
        ArenaLocation arena = new ArenaLocation(
                name,
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );
        try (PreparedStatement ps = database.connection().prepareStatement("""
                INSERT INTO arenas (name, world, x, y, z, yaw, pitch)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(name) DO UPDATE SET
                    world = excluded.world,
                    x = excluded.x,
                    y = excluded.y,
                    z = excluded.z,
                    yaw = excluded.yaw,
                    pitch = excluded.pitch
                """)) {
            ps.setString(1, key);
            ps.setString(2, arena.world());
            ps.setDouble(3, arena.x());
            ps.setDouble(4, arena.y());
            ps.setDouble(5, arena.z());
            ps.setFloat(6, arena.yaw());
            ps.setFloat(7, arena.pitch());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save arena " + name, ex);
            return;
        }
        arenas.put(key, arena);
    }

    public boolean warp(Player player, String arenaName) {
        if (!plugin.pluginConfig().isArenaEnabled()) {
            plugin.messages().send(player, "arena.warp-disabled");
            return false;
        }
        ArenaLocation arena = arenas.get(normalize(arenaName));
        if (arena == null) {
            plugin.messages().send(player, "arena.not-found", Map.of("arena", arenaName));
            return false;
        }
        return teleport(player, arena);
    }

    public boolean warp(Player player) {
        if (!plugin.pluginConfig().isArenaEnabled()) {
            plugin.messages().send(player, "arena.warp-disabled");
            return false;
        }
        if (arenas.isEmpty()) {
            plugin.messages().send(player, "arena.warp-not-set");
            return false;
        }
        new com.arenapvp.arena.ArenaGui(plugin, this).open(player);
        return true;
    }

    public boolean teleport(Player player, ArenaLocation arena) {
        World world = Bukkit.getWorld(arena.world());
        if (world == null) {
            world = Bukkit.getWorld(plugin.pluginConfig().getDefaultWorldFallback());
        }
        if (world == null) {
            plugin.messages().send(player, "arena.world-not-found", Map.of("world", arena.world()));
            return false;
        }
        Location target = new Location(world, arena.x(), arena.y(), arena.z(), arena.yaw(), arena.pitch());
        player.teleport(target);
        plugin.messages().send(player, "arena.warp-success", Map.of("arena", arena.name()));
        return true;
    }

    public List<String> arenaNames() {
        return new ArrayList<>(arenas.values().stream().map(ArenaLocation::name).toList());
    }

    public ArenaLocation getArena(String name) {
        return arenas.get(normalize(name));
    }

    public boolean hasArenas() {
        return !arenas.isEmpty();
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
