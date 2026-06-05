package com.arenapvp.message;

import com.arenapvp.ArenaPVPPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MessageService {

    public static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final ArenaPVPPlugin plugin;
    private FileConfiguration messages;

    public MessageService(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String raw(String path) {
        return messages.getString(path, path);
    }

    public List<String> rawLines(String path) {
        List<String> lines = messages.getStringList(path);
        if (lines.isEmpty()) {
            return List.of(raw(path));
        }
        return lines;
    }

    public String format(String path, Map<String, String> placeholders) {
        String template = raw(path);
        if (plugin.pluginConfig().isPrefixEnabled() && template.contains("{prefix}")) {
            placeholders = new java.util.HashMap<>(placeholders);
            placeholders.putIfAbsent("prefix", raw("prefix"));
        } else {
            template = template.replace("{prefix}", "");
        }
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return color(result);
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(stripColor(format(path, placeholders)));
        } else {
            sender.sendMessage(component(path, placeholders));
        }
    }

    public void send(CommandSender sender, String path) {
        send(sender, path, Map.of());
    }

    public Component component(String path, Map<String, String> placeholders) {
        return LEGACY.deserialize(format(path, placeholders));
    }

    public List<Component> components(String path) {
        List<String> lines = messages.getStringList(path);
        if (lines.isEmpty()) {
            lines = List.of(raw(path));
        }
        List<Component> result = new ArrayList<>();
        String prefix = plugin.pluginConfig().isPrefixEnabled() ? raw("prefix") : "";
        for (String line : lines) {
            String formatted = line.replace("{prefix}", prefix);
            result.add(LEGACY.deserialize(color(formatted)));
        }
        return result;
    }

    public static String color(String input) {
        return input.replace('&', '\u00A7');
    }

    public static String stripColor(String input) {
        return input.replaceAll("(?i)[§&][0-9a-fk-or]", "");
    }
}
