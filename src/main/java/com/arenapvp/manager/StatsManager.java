package com.arenapvp.manager;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.model.PlayerStats;
import com.arenapvp.storage.DatabaseManager;
import com.arenapvp.util.RankCalculator;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class StatsManager {

    private final ArenaPVPPlugin plugin;
    private final DatabaseManager database;
    private final Map<UUID, PlayerStats> cache = new ConcurrentHashMap<>();

    public StatsManager(ArenaPVPPlugin plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
    }

    public PlayerStats getOrCreate(UUID uuid, String name) {
        PlayerStats cached = cache.get(uuid);
        if (cached != null) {
            return cached;
        }
        PlayerStats loaded = loadFromDatabase(uuid);
        if (loaded != null) {
            cache.put(uuid, loaded);
            return loaded;
        }
        PlayerStats created = new PlayerStats(uuid, name, 0, 0, 0, 0);
        persist(created);
        cache.put(uuid, created);
        return created;
    }

    private PlayerStats loadFromDatabase(UUID uuid) {
        try (PreparedStatement ps = database.connection().prepareStatement(
                "SELECT name, kills, deaths, current_streak, best_streak FROM player_stats WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerStats(uuid, rs.getString("name"), rs.getInt("kills"), rs.getInt("deaths"),
                            rs.getInt("current_streak"), rs.getInt("best_streak"));
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to load stats for " + uuid, ex);
        }
        return null;
    }

    public void save(PlayerStats stats) {
        persist(stats);
        cache.put(stats.uuid(), stats);
    }

    private void persist(PlayerStats stats) {
        try (PreparedStatement ps = database.connection().prepareStatement("""
                INSERT INTO player_stats (uuid, name, kills, deaths, current_streak, best_streak)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    name = excluded.name,
                    kills = excluded.kills,
                    deaths = excluded.deaths,
                    current_streak = excluded.current_streak,
                    best_streak = excluded.best_streak
                """)) {
            ps.setString(1, stats.uuid().toString());
            ps.setString(2, stats.name());
            ps.setInt(3, stats.kills());
            ps.setInt(4, stats.deaths());
            ps.setInt(5, stats.currentStreak());
            ps.setInt(6, stats.bestStreak());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save stats for " + stats.uuid(), ex);
        }
    }

    public RankCalculator.RankTier rankFor(PlayerStats stats) {
        if (!plugin.pluginConfig().isRankedEnabled()) {
            return new RankCalculator.RankTier("unranked", 0);
        }
        return RankCalculator.calculate(stats.kills(), plugin.pluginConfig().getRankTiers());
    }

    public List<PlayerStats> topStreaks(int limit) {
        return queryTop("best_streak DESC", limit);
    }

    public List<PlayerStats> topKills(int limit) {
        return queryTop("kills DESC", limit);
    }

    public List<PlayerStats> topDeaths(int limit) {
        return queryTop("deaths DESC", limit);
    }

    private List<PlayerStats> queryTop(String orderBy, int limit) {
        List<PlayerStats> result = new ArrayList<>();
        try (PreparedStatement ps = database.connection().prepareStatement(
                "SELECT uuid, name, kills, deaths, current_streak, best_streak FROM player_stats ORDER BY "
                        + orderBy + " LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PlayerStats(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak")
                    ));
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to load top stats ordered by " + orderBy, ex);
        }
        return result;
    }

    public void handleKill(UUID killerId, String killerName, UUID victimId, String victimName) {
        if (!plugin.pluginConfig().isStatsEnabled()) {
            return;
        }

        PlayerStats killer = getOrCreate(killerId, killerName);
        PlayerStats victim = getOrCreate(victimId, victimName);
        killer.setName(killerName);
        victim.setName(victimName);

        RankCalculator.RankTier oldRank = rankFor(killer);

        if (plugin.pluginConfig().isTrackKills()) {
            killer.setKills(killer.kills() + 1);
        }
        if (plugin.pluginConfig().isTrackDeaths()) {
            victim.setDeaths(victim.deaths() + 1);
        }
        if (plugin.pluginConfig().isTrackStreaks()) {
            killer.setCurrentStreak(killer.currentStreak() + 1);
            if (killer.currentStreak() > killer.bestStreak()) {
                killer.setBestStreak(killer.currentStreak());
            }
            if (plugin.pluginConfig().isResetStreakOnDeath()) {
                victim.setCurrentStreak(0);
            }
        }

        save(killer);
        save(victim);

        RankCalculator.RankTier newRank = rankFor(killer);
        if (plugin.pluginConfig().isRankedEnabled() && !oldRank.displayName().equals(newRank.displayName())) {
            var player = Bukkit.getPlayer(killerId);
            if (player != null) {
                plugin.messages().send(player, "pvp.rank-up", Map.of("rank", newRank.displayName()));
            }
        }
    }

    public void handleDeath(UUID victimId, String victimName) {
        if (!plugin.pluginConfig().isStatsEnabled()) {
            return;
        }
        PlayerStats victim = getOrCreate(victimId, victimName);
        victim.setName(victimName);
        if (plugin.pluginConfig().isTrackDeaths()) {
            victim.setDeaths(victim.deaths() + 1);
        }
        if (plugin.pluginConfig().isTrackStreaks() && plugin.pluginConfig().isResetStreakOnDeath()) {
            victim.setCurrentStreak(0);
        }
        save(victim);
    }
}
