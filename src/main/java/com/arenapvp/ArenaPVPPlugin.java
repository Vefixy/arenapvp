package com.arenapvp;

import com.arenapvp.command.ArenaCommand;
import com.arenapvp.config.PluginConfig;
import com.arenapvp.hook.EconomyHook;
import com.arenapvp.hook.EssentialsHook;
import com.arenapvp.hook.PlaceholderHook;
import com.arenapvp.kit.KitManager;
import com.arenapvp.listener.CombatListener;
import com.arenapvp.listener.JoinListener;
import com.arenapvp.manager.ArenaManager;
import com.arenapvp.manager.StatsManager;
import com.arenapvp.message.MessageService;
import com.arenapvp.storage.DatabaseManager;
import com.arenapvp.update.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ArenaPVPPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private MessageService messageService;
    private DatabaseManager databaseManager;
    private StatsManager statsManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private EconomyHook economyHook;
    private EssentialsHook essentialsHook;
    private PlaceholderHook placeholderHook;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        pluginConfig = new PluginConfig(this);
        messageService = new MessageService(this);
        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        statsManager = new StatsManager(this, databaseManager);
        arenaManager = new ArenaManager(this, databaseManager);
        kitManager = new KitManager(this, databaseManager);

        economyHook = new EconomyHook(this);
        economyHook.setup();
        essentialsHook = new EssentialsHook(this);
        placeholderHook = new PlaceholderHook(this);
        placeholderHook.register();

        updateChecker = new UpdateChecker(this);
        if (pluginConfig.isUpdateCheckOnStartup()) {
            getServer().getScheduler().runTaskAsynchronously(this, updateChecker::checkConsole);
        }
        updateChecker.startPeriodicTask();

        registerCommand("arenapvp", "Main ArenaPVP command", List.of("apvp", "arena"), new ArenaCommand(this));

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        getLogger().info("ArenaPVP v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (placeholderHook != null) {
            placeholderHook.unregister();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("ArenaPVP disabled.");
    }

    public void reloadAll() {
        reloadConfig();
        pluginConfig.reload();
        messageService.reload();
        arenaManager.reload();
        kitManager.reload();
        economyHook.setup();
        essentialsHook.reload();
        placeholderHook.register();
        updateChecker.resetCache();
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public MessageService messages() {
        return messageService;
    }

    public DatabaseManager database() {
        return databaseManager;
    }

    public StatsManager stats() {
        return statsManager;
    }

    public ArenaManager arena() {
        return arenaManager;
    }

    public KitManager kits() {
        return kitManager;
    }

    public EconomyHook economy() {
        return economyHook;
    }

    public EssentialsHook essentials() {
        return essentialsHook;
    }

    public UpdateChecker updates() {
        return updateChecker;
    }
}
