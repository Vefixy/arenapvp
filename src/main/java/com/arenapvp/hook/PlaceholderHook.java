package com.arenapvp.hook;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.model.PlayerStats;
import com.arenapvp.util.RankCalculator;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public final class PlaceholderHook {

    private final ArenaPVPPlugin plugin;
    private ArenaExpansion expansion;

    public PlaceholderHook(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (!plugin.pluginConfig().isPlaceholdersEnabled() || Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        if (expansion != null) {
            expansion.unregister();
        }
        expansion = new ArenaExpansion();
        expansion.register();
    }

    public void unregister() {
        if (expansion != null) {
            expansion.unregister();
        }
    }

    private final class ArenaExpansion extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "arenapvp";
        }

        @Override
        public String getAuthor() {
            return "ArenaPVP";
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onRequest(OfflinePlayer player, String params) {
            if (player == null) {
                return handleTopPlaceholder(params);
            }
            PlayerStats stats = plugin.stats().getOrCreate(player.getUniqueId(), player.getName() == null ? "unknown" : player.getName());
            return switch (params.toLowerCase()) {
                case "kills" -> String.valueOf(stats.kills());
                case "deaths" -> String.valueOf(stats.deaths());
                case "kdr" -> String.valueOf(RankCalculator.kdr(stats.kills(), stats.deaths()));
                case "streak" -> String.valueOf(stats.currentStreak());
                case "best_streak" -> String.valueOf(stats.bestStreak());
                case "rank" -> plugin.stats().rankFor(stats).displayName();
                default -> handleTopPlaceholder(params);
            };
        }

        private String handleTopPlaceholder(String params) {
            if (!params.startsWith("top_streak_")) {
                return "";
            }
            String[] parts = params.split("_");
            if (parts.length < 4) {
                return "";
            }
            try {
                int position = Integer.parseInt(parts[2]);
                String field = parts[3];
                List<PlayerStats> top = plugin.stats().topStreaks(plugin.pluginConfig().getTopStreakSize());
                if (position < 1 || position > top.size()) {
                    return "";
                }
                PlayerStats entry = top.get(position - 1);
                return switch (field) {
                    case "name" -> entry.name();
                    case "value" -> String.valueOf(entry.bestStreak());
                    case "kills" -> String.valueOf(entry.kills());
                    default -> "";
                };
            } catch (NumberFormatException ex) {
                return "";
            }
        }
    }
}
