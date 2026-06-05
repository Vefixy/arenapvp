package com.arenapvp.command;

import com.arenapvp.ArenaPVPPlugin;
import com.arenapvp.message.MessageService;
import com.arenapvp.model.PlayerStats;
import com.arenapvp.util.RankCalculator;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ArenaCommand implements BasicCommand {

    private static final List<String> SUBCOMMANDS = List.of(
            "warp", "setarena", "kit", "stats", "reload", "update", "help"
    );

    private final ArenaPVPPlugin plugin;

    public ArenaCommand(ArenaPVPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "warp", "arena" -> handleWarp(sender, args);
            case "setarena" -> handleSetArena(sender, args);
            case "reload" -> handleReload(sender);
            case "kit" -> handleKit(sender, args);
            case "stats" -> handleStats(sender, args);
            case "update" -> handleUpdate(sender);
            case "help" -> sendHelp(sender);
            default -> plugin.messages().send(sender, "general.unknown-subcommand");
        }
    }

    private void handleWarp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "general.player-only");
            return;
        }
        if (!player.hasPermission("arenapvp.warp")) {
            plugin.messages().send(sender, "general.no-permission");
            return;
        }
        if (args.length >= 2) {
            plugin.arena().warp(player, args[1]);
        } else {
            plugin.arena().warp(player);
        }
    }

    private void handleSetArena(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "general.player-only");
            return;
        }
        if (!player.hasPermission("arenapvp.setarena")) {
            plugin.messages().send(sender, "general.no-permission");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /arenapvp setarena <name>");
            return;
        }
        String name = args[1];
        plugin.arena().setArena(player, name);
        var loc = player.getLocation();
        plugin.messages().send(player, "arena.set-success", Map.of(
                "arena", name,
                "world", loc.getWorld().getName(),
                "x", String.format("%.1f", loc.getX()),
                "y", String.format("%.1f", loc.getY()),
                "z", String.format("%.1f", loc.getZ())
        ));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("arenapvp.reload")) {
            plugin.messages().send(sender, "general.no-permission");
            return;
        }
        plugin.reloadAll();
        plugin.messages().send(sender, "general.reload-success");
    }

    private void handleKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.messages().send(sender, "general.player-only");
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("create")) {
            if (!player.hasPermission("arenapvp.kit.create") || args.length < 3) {
                plugin.messages().send(sender, "general.no-permission");
                return;
            }
            plugin.kits().createLocalKit(player, args[2]);
            return;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("arenapvp.kit.delete") || args.length < 3) {
                plugin.messages().send(sender, "general.no-permission");
                return;
            }
            plugin.kits().deleteLocalKit(player, args[2]);
            return;
        }
        if (!player.hasPermission("arenapvp.kit")) {
            plugin.messages().send(sender, "general.no-permission");
            return;
        }
        if (args.length >= 2) {
            plugin.kits().giveKit(player, args[1]);
            return;
        }
        plugin.essentials().warnIfNeeded(player);
        plugin.kits().openGui(player);
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("arenapvp.stats")) {
            plugin.messages().send(sender, "general.no-permission");
            return;
        }
        if (!plugin.pluginConfig().isStatsEnabled()) {
            plugin.messages().send(sender, "stats.disabled");
            return;
        }

        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("arenapvp.stats.others")) {
                plugin.messages().send(sender, "general.no-permission");
                return;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.messages().send(sender, "stats.player-not-found", Map.of("player", args[1]));
                return;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("Usage: /arenapvp stats <player>");
            return;
        }

        PlayerStats stats = plugin.stats().getOrCreate(target.getUniqueId(), target.getName());
        String rank = plugin.stats().rankFor(stats).displayName();
        Map<String, String> placeholders = Map.of(
                "player", target.getName(),
                "kills", String.valueOf(stats.kills()),
                "deaths", String.valueOf(stats.deaths()),
                "kdr", String.valueOf(RankCalculator.kdr(stats.kills(), stats.deaths())),
                "streak", String.valueOf(stats.currentStreak()),
                "rank", rank
        );
        if (target.equals(sender)) {
            plugin.messages().send(sender, "stats.self", placeholders);
        } else {
            plugin.messages().send(sender, "stats.other", placeholders);
        }
    }

    private void handleUpdate(CommandSender sender) {
        if (!sender.hasPermission("arenapvp.update")) {
            plugin.messages().send(sender, "general.no-permission");
            return;
        }
        if (sender instanceof Player player) {
            plugin.updates().checkCommand(player);
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, plugin.updates()::checkConsole);
        }
    }

    private void sendHelp(CommandSender sender) {
        if (!sender.hasPermission("arenapvp.help")) {
            plugin.messages().send(sender, "general.no-permission");
            return;
        }
        String prefix = plugin.pluginConfig().isPrefixEnabled() ? plugin.messages().raw("prefix") : "";
        if (sender instanceof ConsoleCommandSender) {
            for (String line : plugin.messages().rawLines("help")) {
                sender.sendMessage(MessageService.stripColor(line.replace("{prefix}", prefix)));
            }
            return;
        }
        for (var line : plugin.messages().components("help")) {
            sender.sendMessage(line);
        }
    }

    @Override
    public List<String> suggest(CommandSourceStack stack, String[] args) {
        if (args.length <= 1) {
            return filter(SUBCOMMANDS, args.length == 0 ? "" : args[0]);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            return switch (sub) {
                case "warp" -> filter(plugin.arena().arenaNames(), args[1]);
                case "setarena" -> List.of();
                case "kit" -> {
                    List<String> options = new ArrayList<>(List.of("create", "delete"));
                    options.addAll(plugin.kits().availableKitNames());
                    yield filter(options, args[1]);
                }
                case "stats" -> filterOnlinePlayers(args[1]);
                default -> List.of();
            };
        }
        if (args.length == 3 && sub.equals("kit")) {
            if (args[1].equalsIgnoreCase("delete")) {
                return filter(plugin.kits().localKitNames(), args[2]);
            }
            if (args[1].equalsIgnoreCase("create")) {
                return List.of();
            }
        }
        return List.of();
    }

    private List<String> filterOnlinePlayers(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lower))
                .toList();
    }

    private static List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
