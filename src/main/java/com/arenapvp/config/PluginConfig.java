package com.arenapvp.config;

import com.arenapvp.ArenaPVPPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PluginConfig {

    private final ArenaPVPPlugin plugin;

    private boolean prefixEnabled;
    private boolean debug;
    private boolean arenaEnabled;
    private String defaultWorldFallback;
    private String arenaGuiTitle;
    private int arenaGuiRows;
    private boolean kitsEnabled;
    private String kitsMode;
    private String essentialsPrefix;
    private String kitGuiTitle;
    private int kitGuiRows;
    private boolean economyEnabled;
    private double killReward;
    private double deathPenalty;
    private double minimumBalance;
    private boolean rankedEnabled;
    private Map<String, List<Integer>> rankTiers;
    private boolean statsEnabled;
    private boolean trackKills;
    private boolean trackDeaths;
    private boolean trackStreaks;
    private boolean resetStreakOnDeath;
    private boolean placeholdersEnabled;
    private int topListSize;
    private boolean actionBarEnabled;
    private List<String> actionBarWorlds;
    private int actionBarUpdateIntervalTicks;
    private int actionBarKillRewardSeconds;
    private String actionBarPermission;
    private boolean economyChatOnKill;
    private boolean updatesEnabled;
    private String githubOwner;
    private String githubRepo;
    private boolean updateCheckOnStartup;
    private boolean notifyOpsOnJoin;
    private int recheckIntervalMinutes;
    private String databaseFilename;

    public PluginConfig(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        var cfg = plugin.getConfig();

        prefixEnabled = cfg.getBoolean("settings.prefix-enabled", true);
        debug = cfg.getBoolean("settings.debug", false);
        arenaEnabled = cfg.getBoolean("arena.enabled", true);
        defaultWorldFallback = cfg.getString("arena.default-world-fallback", "world");
        arenaGuiTitle = cfg.getString("arena.gui-title", "&c&lArena Warps");
        arenaGuiRows = cfg.getInt("arena.gui-rows", 3);
        kitsEnabled = cfg.getBoolean("kits.enabled", true);
        kitsMode = cfg.getString("kits.mode", "both");
        essentialsPrefix = cfg.getString("kits.essentials-prefix", "");
        kitGuiTitle = cfg.getString("kits.gui-title", "&c&lArena Kits");
        kitGuiRows = cfg.getInt("kits.gui-rows", 3);
        economyEnabled = cfg.getBoolean("economy.enabled", true);
        killReward = cfg.getDouble("economy.kill-reward", 50.0);
        deathPenalty = cfg.getDouble("economy.death-penalty", 25.0);
        minimumBalance = cfg.getDouble("economy.minimum-balance", 0.0);
        rankedEnabled = cfg.getBoolean("ranked.enabled", true);
        rankTiers = parseTiers(cfg.getConfigurationSection("ranked.tiers"));
        statsEnabled = cfg.getBoolean("stats.enabled", true);
        trackKills = cfg.getBoolean("stats.track-kills", true);
        trackDeaths = cfg.getBoolean("stats.track-deaths", true);
        trackStreaks = cfg.getBoolean("stats.track-streaks", true);
        resetStreakOnDeath = cfg.getBoolean("stats.reset-streak-on-death", true);
        placeholdersEnabled = cfg.getBoolean("placeholders.enabled", true);
        topListSize = cfg.getInt("placeholders.top-list-size", cfg.getInt("placeholders.top-streak-size", 10));
        actionBarEnabled = cfg.getBoolean("action-bar.enabled", true);
        actionBarWorlds = cfg.getStringList("action-bar.worlds");
        actionBarUpdateIntervalTicks = cfg.getInt("action-bar.update-interval-ticks", 20);
        actionBarKillRewardSeconds = cfg.getInt("action-bar.kill-reward-display-seconds", 4);
        actionBarPermission = cfg.getString("action-bar.permission", "arenapvp.actionbar");
        economyChatOnKill = cfg.getBoolean("economy.chat-message-on-kill", false);
        updatesEnabled = cfg.getBoolean("updates.enabled", true);
        githubOwner = cfg.getString("updates.github-owner", "Vefixy");
        githubRepo = cfg.getString("updates.github-repo", "arenapvp");
        updateCheckOnStartup = cfg.getBoolean("updates.check-on-startup", true);
        notifyOpsOnJoin = cfg.getBoolean("updates.notify-ops-on-join", true);
        recheckIntervalMinutes = cfg.getInt("updates.recheck-interval-minutes", 60);
        databaseFilename = cfg.getString("database.filename", "arenapvp.db");
    }

    private static Map<String, List<Integer>> parseTiers(ConfigurationSection section) {
        Map<String, List<Integer>> tiers = new LinkedHashMap<>();
        if (section == null) {
            return tiers;
        }
        for (String key : section.getKeys(false)) {
            tiers.put(key.toLowerCase(), section.getIntegerList(key));
        }
        return tiers;
    }

    public boolean isPrefixEnabled() { return prefixEnabled; }
    public boolean isDebug() { return debug; }
    public boolean isArenaEnabled() { return arenaEnabled; }
    public String getDefaultWorldFallback() { return defaultWorldFallback; }
    public String getArenaGuiTitle() { return arenaGuiTitle; }
    public int getArenaGuiRows() { return arenaGuiRows; }
    public boolean isKitsEnabled() { return kitsEnabled; }
    public String getKitsMode() { return kitsMode; }
    public String getEssentialsPrefix() { return essentialsPrefix; }
    public String getKitGuiTitle() { return kitGuiTitle; }
    public int getKitGuiRows() { return kitGuiRows; }
    public boolean isEconomyEnabled() { return economyEnabled; }
    public double getKillReward() { return killReward; }
    public double getDeathPenalty() { return deathPenalty; }
    public double getMinimumBalance() { return minimumBalance; }
    public boolean isRankedEnabled() { return rankedEnabled; }
    public Map<String, List<Integer>> getRankTiers() { return rankTiers; }
    public boolean isStatsEnabled() { return statsEnabled; }
    public boolean isTrackKills() { return trackKills; }
    public boolean isTrackDeaths() { return trackDeaths; }
    public boolean isTrackStreaks() { return trackStreaks; }
    public boolean isResetStreakOnDeath() { return resetStreakOnDeath; }
    public boolean isPlaceholdersEnabled() { return placeholdersEnabled; }
    public int getTopListSize() { return topListSize; }
    public boolean isActionBarEnabled() { return actionBarEnabled; }
    public List<String> getActionBarWorlds() { return actionBarWorlds; }
    public int getActionBarUpdateIntervalTicks() { return actionBarUpdateIntervalTicks; }
    public int getActionBarKillRewardSeconds() { return actionBarKillRewardSeconds; }
    public String getActionBarPermission() { return actionBarPermission; }
    public boolean isEconomyChatOnKill() { return economyChatOnKill; }
    public boolean isUpdatesEnabled() { return updatesEnabled; }
    public String getGithubOwner() { return githubOwner; }
    public String getGithubRepo() { return githubRepo; }
    public boolean isUpdateCheckOnStartup() { return updateCheckOnStartup; }
    public boolean isNotifyOpsOnJoin() { return notifyOpsOnJoin; }
    public int getRecheckIntervalMinutes() { return recheckIntervalMinutes; }
    public String getDatabaseFilename() { return databaseFilename; }
}
