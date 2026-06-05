package com.arenapvp.listener;

import com.arenapvp.ArenaPVPPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {

    private final ArenaPVPPlugin plugin;

    public JoinListener(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.pluginConfig().isNotifyOpsOnJoin()) {
            return;
        }
        plugin.updates().checkAndNotify(event.getPlayer());
    }
}
