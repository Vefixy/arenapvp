package com.arenapvp.hook;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.model.PlayerStats;
import com.arenapvp.util.RankCalculator;
import com.arenapvp.util.TopPlaceholderParser;
import com.arenapvp.util.TopPlaceholderParser.TopCategory;
import com.arenapvp.util.TopPlaceholderParser.TopField;
import com.arenapvp.util.TopPlaceholderParser.TopRequest;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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
            if (player != null) {
                PlayerStats stats = plugin.stats().getOrCreate(
                        player.getUniqueId(),
                        player.getName() == null ? "unknown" : player.getName()
                );
                return switch (params.toLowerCase()) {
                    case "kills" -> String.valueOf(stats.kills());
                    case "deaths" -> String.valueOf(stats.deaths());
                    case "kdr" -> String.valueOf(RankCalculator.kdr(stats.kills(), stats.deaths()));
                    case "streak" -> String.valueOf(stats.currentStreak());
                    case "best_streak" -> String.valueOf(stats.bestStreak());
                    case "rank" -> plugin.stats().rankFor(stats).displayName();
                    default -> resolveTopPlaceholder(params);
                };
            }
            return resolveTopPlaceholder(params);
        }

        private String resolveTopPlaceholder(String params) {
            TopRequest request = TopPlaceholderParser.parse(params);
            if (request == null) {
                return "";
            }
            int limit = plugin.pluginConfig().getTopListSize();
            if (request.position() < 1 || request.position() > limit) {
                return "";
            }
            List<PlayerStats> top = switch (request.category()) {
                case KILLS -> plugin.stats().topKills(limit);
                case DEATHS -> plugin.stats().topDeaths(limit);
                case STREAK -> plugin.stats().topStreaks(limit);
            };
            if (request.position() > top.size()) {
                return emptyTopValue(request.field());
            }
            PlayerStats entry = top.get(request.position() - 1);
            return switch (request.field()) {
                case NAME -> entry.name();
                case VALUE -> switch (request.category()) {
                    case KILLS -> String.valueOf(entry.kills());
                    case DEATHS -> String.valueOf(entry.deaths());
                    case STREAK -> String.valueOf(entry.bestStreak());
                };
                case KILLS -> String.valueOf(entry.kills());
                default -> "";
            };
        }

        private static String emptyTopValue(TopField field) {
            return field == TopField.NAME ? "---" : "0";
        }
    }
}
