package com.arenapvp.hook;

import com.arenapvp.ArenaPVPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class EssentialsHook {

    private final ArenaPVPPlugin plugin;
    private Plugin essentialsPlugin;
    private Object essentials;
    private Object kits;

    public EssentialsHook(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        essentials = essentialsPlugin;
        kits = null;
        if (essentials != null) {
            try {
                Method getKits = essentials.getClass().getMethod("getKits");
                kits = getKits.invoke(essentials);
            } catch (ReflectiveOperationException ex) {
                plugin.getLogger().log(Level.WARNING, "Could not access Essentials kits API", ex);
            }
        }
    }

    public boolean isAvailable() {
        return essentials != null && kits != null;
    }

    @SuppressWarnings("unchecked")
    public List<String> listKits() {
        if (!isAvailable()) {
            return List.of();
        }
        try {
            Method getKitKeys = kits.getClass().getMethod("getKitKeys");
            Collection<String> keys = (Collection<String>) getKitKeys.invoke(kits);
            String prefix = plugin.pluginConfig().getEssentialsPrefix();
            List<String> result = new ArrayList<>();
            for (String kitName : keys) {
                if (prefix == null || prefix.isEmpty()
                        || kitName.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
                    result.add(kitName);
                }
            }
            return result;
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to list Essentials kits", ex);
            return List.of();
        }
    }

    public boolean giveKit(Player player, String kitName) {
        if (!isAvailable()) {
            return false;
        }
        try {
            Method getUser = essentials.getClass().getMethod("getUser", Player.class);
            Object user = getUser.invoke(essentials, player);
            Method getKit = kits.getClass().getMethod("getKit", String.class);
            Object kit = getKit.invoke(kits, kitName);
            if (kit == null) {
                return false;
            }
            Method expandItems = kit.getClass().getMethod("expandItems", user.getClass().getInterfaces()[0]);
            for (Method method : kit.getClass().getMethods()) {
                if (method.getName().equals("expandItems") && method.getParameterCount() == 1) {
                    expandItems = method;
                    break;
                }
            }
            expandItems.invoke(kit, user);
            Method equipKit = kit.getClass().getMethod("equipKit", user.getClass().getInterfaces().length > 0
                    ? user.getClass().getInterfaces()[0]
                    : user.getClass());
            for (Method method : kit.getClass().getMethods()) {
                if (method.getName().equals("equipKit") && method.getParameterCount() == 1) {
                    equipKit = method;
                    break;
                }
            }
            equipKit.invoke(kit, user);
            return true;
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to give Essentials kit " + kitName, ex);
            return false;
        }
    }

    public void warnIfNeeded(Player player) {
        String mode = plugin.pluginConfig().getKitsMode().toLowerCase(Locale.ROOT);
        if ((mode.equals("essentials") || mode.equals("both")) && !isAvailable()) {
            plugin.messages().send(player, "kits.essentials-unavailable");
        }
    }
}
