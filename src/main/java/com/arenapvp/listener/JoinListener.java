package com.arenapvp.listener;

import com.arenapvp.ArenaPVPPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinListener implements Listener {

    private final ArenaPVPPlugin plugin;

    public JoinListener(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.actionBar().onJoin(event.getPlayer());
        if (plugin.pluginConfig().isNotifyOpsOnJoin()) {
            plugin.updates().checkAndNotify(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.actionBar().onQuit(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.actionBar().onJoin(event.getPlayer());
    }
}
