package com.arenapvp.hook;

import com.arenapvp.ArenaPVPPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Map;

public final class EconomyHook {

    private final ArenaPVPPlugin plugin;
    private Economy economy;

    public EconomyHook(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            economy = null;
            return;
        }
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = provider == null ? null : provider.getProvider();
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public void rewardKill(Player killer, Player victim) {
        if (!plugin.pluginConfig().isEconomyEnabled() || !isAvailable()) {
            return;
        }
        double reward = plugin.pluginConfig().getKillReward();
        if (reward <= 0) {
            return;
        }
        economy.depositPlayer(killer, reward);
        plugin.messages().send(killer, "economy.kill-reward", Map.of(
                "amount", format(reward),
                "victim", victim.getName()
        ));
    }

    public void penalizeDeath(Player victim, Player killer) {
        if (!plugin.pluginConfig().isEconomyEnabled() || !isAvailable()) {
            return;
        }
        double penalty = plugin.pluginConfig().getDeathPenalty();
        if (penalty <= 0) {
            return;
        }
        double balance = economy.getBalance(victim);
        double min = plugin.pluginConfig().getMinimumBalance();
        double actual = Math.min(penalty, Math.max(0, balance - min));
        if (actual <= 0) {
            return;
        }
        economy.withdrawPlayer(victim, actual);
        plugin.messages().send(victim, "economy.death-penalty", Map.of(
                "amount", format(actual),
                "killer", killer == null ? "unknown" : killer.getName()
        ));
    }

    private String format(double amount) {
        return String.format("%.2f", amount);
    }
}
