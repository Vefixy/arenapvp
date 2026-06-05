package com.arenapvp.update;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.util.VersionComparator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public final class UpdateChecker {

    private final ArenaPVPPlugin plugin;
    private final AtomicReference<String> latestVersion = new AtomicReference<>();
    private final AtomicReference<String> downloadUrl = new AtomicReference<>();

    public UpdateChecker(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    public void startPeriodicTask() {
        int minutes = plugin.pluginConfig().getRecheckIntervalMinutes();
        if (!plugin.pluginConfig().isUpdatesEnabled() || minutes <= 0) {
            return;
        }
        long ticks = minutes * 60L * 20L;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkConsole, ticks, ticks);
    }

    public void resetCache() {
        latestVersion.set(null);
        downloadUrl.set(null);
    }

    public void checkConsole() {
        if (!plugin.pluginConfig().isUpdatesEnabled()) {
            return;
        }
        UpdateResult result = fetchLatest();
        String current = plugin.getDescription().getVersion();
        if (result == null) {
            plugin.getLogger().warning("Update check failed — see previous log entries.");
            return;
        }
        if (VersionComparator.isNewerAvailable(current, result.version())) {
            plugin.messages().send(Bukkit.getConsoleSender(), "updates.console-available", Map.of(
                    "latest", result.version(),
                    "current", current,
                    "url", result.url()
            ));
        } else {
            plugin.getLogger().info(plugin.messages().format("updates.console-up-to-date", Map.of("version", current)));
        }
    }

    public void checkAndNotify(Player player) {
        if (!plugin.pluginConfig().isUpdatesEnabled() || !player.isOp()) {
            return;
        }
        if (!player.hasPermission("arenapvp.update.notify")) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UpdateResult result = fetchLatest();
            String current = plugin.getDescription().getVersion();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (result == null) {
                    plugin.messages().send(player, "updates.failed");
                    return;
                }
                if (VersionComparator.isNewerAvailable(current, result.version())) {
                    plugin.messages().send(player, "updates.available", Map.of(
                            "latest", result.version(),
                            "current", current,
                            "url", result.url()
                    ));
                } else {
                    plugin.messages().send(player, "updates.up-to-date", Map.of("version", current));
                }
            });
        });
    }

    public void checkCommand(Player player) {
        plugin.messages().send(player, "updates.checking");
        checkAndNotify(player);
    }

    private UpdateResult fetchLatest() {
        String cached = latestVersion.get();
        if (cached != null) {
            return new UpdateResult(cached, downloadUrl.get());
        }
        String owner = plugin.pluginConfig().getGithubOwner();
        String repo = plugin.pluginConfig().getGithubRepo();
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest";
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", "ArenaPVP-UpdateChecker");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            if (connection.getResponseCode() != 200) {
                plugin.getLogger().warning("GitHub API returned HTTP " + connection.getResponseCode() + " for " + apiUrl);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                String body = json.toString();
                String tag = extractJsonString(body, "tag_name");
                String htmlUrl = extractJsonString(body, "html_url");
                if (tag == null) {
                    return null;
                }
                latestVersion.set(tag);
                downloadUrl.set(htmlUrl == null ? "https://github.com/" + owner + "/" + repo + "/releases" : htmlUrl);
                return new UpdateResult(tag, downloadUrl.get());
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to check updates from GitHub", ex);
            return null;
        }
    }

    private static String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            return null;
        }
        return json.substring(start, end);
    }

    public record UpdateResult(String version, String url) {
    }
}
