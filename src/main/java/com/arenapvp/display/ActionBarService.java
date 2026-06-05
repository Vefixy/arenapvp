package com.arenapvp.display;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.model.PlayerStats;
import com.arenapvp.util.RankCalculator;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionBarService {

    private final ArenaPVPPlugin plugin;
    private final Map<UUID, KillOverlay> overlays = new ConcurrentHashMap<>();
    private BukkitTask task;

    public ActionBarService(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        if (!plugin.pluginConfig().isActionBarEnabled()) {
            return;
        }
        int interval = plugin.pluginConfig().getActionBarUpdateIntervalTicks();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        overlays.clear();
    }

    public void reload() {
        start();
    }

    public void onJoin(Player player) {
        if (!shouldShow(player)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> update(player), 10L);
    }

    public void onQuit(Player player) {
        overlays.remove(player.getUniqueId());
    }

    public void showKillReward(Player killer, double amount, String victim) {
        if (!shouldShow(killer)) {
            return;
        }
        long until = System.currentTimeMillis() + plugin.pluginConfig().getActionBarKillRewardSeconds() * 1000L;
        overlays.put(killer.getUniqueId(), new KillOverlay(amount, victim, until));
        update(killer);
    }

    private void tick() {
        long now = System.currentTimeMillis();
        overlays.entrySet().removeIf(entry -> entry.getValue().untilMs() <= now);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (shouldShow(player)) {
                update(player);
            }
        }
    }

    private void update(Player player) {
        Component stats = plugin.messages().component("action-bar.stats", statsPlaceholders(player));
        KillOverlay overlay = overlays.get(player.getUniqueId());
        if (overlay != null && overlay.untilMs() > System.currentTimeMillis()) {
            Map<String, String> placeholders = statsPlaceholders(player);
            String reward = plugin.messages().format("action-bar.kill-reward", Map.of(
                    "amount", formatMoney(overlay.amount()),
                    "victim", overlay.victim()
            ));
            String statsLine = plugin.messages().format("action-bar.stats", placeholders);
            player.sendActionBar(plugin.messages().component("action-bar.combined", Map.of(
                    "reward", reward,
                    "stats", statsLine
            )));
            return;
        }
        player.sendActionBar(stats);
    }

    private Map<String, String> statsPlaceholders(Player player) {
        PlayerStats stats = plugin.stats().getOrCreate(player.getUniqueId(), player.getName());
        return Map.of(
                "kills", String.valueOf(stats.kills()),
                "deaths", String.valueOf(stats.deaths()),
                "kdr", String.valueOf(RankCalculator.kdr(stats.kills(), stats.deaths())),
                "streak", String.valueOf(stats.currentStreak()),
                "rank", plugin.stats().rankFor(stats).displayName()
        );
    }

    private boolean shouldShow(Player player) {
        if (!plugin.pluginConfig().isActionBarEnabled()) {
            return false;
        }
        if (!player.hasPermission(plugin.pluginConfig().getActionBarPermission())) {
            return false;
        }
        List<String> worlds = plugin.pluginConfig().getActionBarWorlds();
        return worlds.isEmpty() || worlds.contains(player.getWorld().getName());
    }

    private static String formatMoney(double amount) {
        if (amount == Math.rint(amount)) {
            return String.valueOf((long) amount);
        }
        return String.format("%.2f", amount);
    }

    private record KillOverlay(double amount, String victim, long untilMs) {
    }
}
