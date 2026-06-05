package com.arenapvp.listener;

import com.arenapvp.ArenaPVPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

public final class CombatListener implements Listener {

    private final ArenaPVPPlugin plugin;

    public CombatListener(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.equals(victim)) {
            plugin.stats().handleKill(killer.getUniqueId(), killer.getName(), victim.getUniqueId(), victim.getName());
            plugin.economy().rewardKill(killer, victim);
            plugin.economy().penalizeDeath(victim, killer);
            if (plugin.pluginConfig().isTrackStreaks()) {
                var stats = plugin.stats().getOrCreate(killer.getUniqueId(), killer.getName());
                plugin.messages().send(killer, "pvp.kill-broadcast", Map.of(
                        "killer", killer.getName(),
                        "victim", victim.getName(),
                        "streak", String.valueOf(stats.currentStreak())
                ));
            }
            return;
        }

        plugin.stats().handleDeath(victim.getUniqueId(), victim.getName());
        plugin.economy().penalizeDeath(victim, null);
    }
}
