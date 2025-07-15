package dev.yours4nty.ultimatebackpacks.commands;

// Java Imports
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// Bukkit & BungeeCord imports
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

// Plugin Imports
import dev.yours4nty.ultimatebackpacks.*;
import dev.yours4nty.ultimatebackpacks.utils.*;

public class AdminBackpackCommand implements CommandExecutor {

    private final UltimateBackpacks plugin;

    public AdminBackpackCommand(UltimateBackpacks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimatebackpacks.admin")) {
            sender.sendMessage(MessageHandler.get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "reload" -> {
                plugin.reloadConfig();
                Config.load(plugin.getConfig());
                MessageHandler.loadMessages();

                String message = ChatColor.DARK_PURPLE + "[UltimateBackpacks] " + ChatColor.GREEN + MessageHandler.get("reload-success");
                sender.sendMessage(message);
                Bukkit.getConsoleSender().sendMessage(message);
            }
            case "view" -> {
                if (args.length < 3) {
                    sender.sendMessage(MessageHandler.get("usage-view"));
                    return true;
                }

                if (!(sender instanceof Player viewer)) {
                    sender.sendMessage(MessageHandler.get("ingame-only"));
                    return true;
                }

                if (WorldUtils.isBlacklisted(viewer.getWorld())) {
                    viewer.sendMessage(MessageHandler.get("disabled-in-world"));
                    return true;
                }

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                    sender.sendMessage(MessageHandler.get("player-not-found"));
                    return true;
                }

                try {
                    int index = Integer.parseInt(args[2]);
                    if (index < 1 || index > 10) {
                        viewer.sendMessage(MessageHandler.get("number-out-of-range"));
                        return true;
                    }
                    BackpackStorage.openBackpack(viewer, target.getUniqueId(), index);
                } catch (NumberFormatException e) {
                    viewer.sendMessage(MessageHandler.get("invalid-number"));
                }
            }
            case "viewshared" -> {
                if (args.length < 2) {
                    sender.sendMessage(MessageHandler.get("usage-viewshared"));
                    return true;
                }

                if (!(sender instanceof Player viewer)) {
                    sender.sendMessage(MessageHandler.get("ingame-only"));
                    return true;
                }

                if (WorldUtils.isBlacklisted(viewer.getWorld())) {
                    viewer.sendMessage(MessageHandler.get("disabled-in-world"));
                    return true;
                }

                String name = args[1];
                File file = new File("plugins/UltimateBackpacks/sharedBackpacks", name + ".yml");

                if (!file.exists()) {
                    sender.sendMessage(MessageHandler.get("shared-backpack-does-not-exist"));
                    return true;
                }

                Inventory sharedInv = Bukkit.createInventory(null, 54,
                        ChatColor.translateAlternateColorCodes('&',
                                MessageHandler.get("shared-backpack-title").replace("%name%", name)));

                sharedInv.setContents(SharedBackpackStorage.getContents(name));
                viewer.openInventory(sharedInv);
            }
            case "log" -> {
                if (args.length == 1) {
                    listLogs(sender);
                    return true;
                }

                if (args.length >= 3 && args[1].equalsIgnoreCase("view")) {
                    String date = args[2];
                    String filter = null;
                    int page = 1;

                    if (args.length >= 4) {
                        try {
                            page = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            filter = args[3];
                        }
                    }

                    if (args.length >= 5) {
                        try {
                            page = Integer.parseInt(args[4]);
                        } catch (NumberFormatException ignored) {}
                    }

                    showLog(sender, date, filter, page);
                }
            }
            default -> sender.sendMessage(MessageHandler.get("invalid-subcommand"));
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§dUltimateBackpacks - Admin Help:");
        sender.sendMessage("§b/ubp reload §7- Reload plugin settings");
        sender.sendMessage("§b/ubp view <player> [number] §7- View a player's backpack");
        sender.sendMessage("§b/ubp viewshared <name> §7- View a shared backpack");
        sender.sendMessage("§b/ubp log §7- List available log files");
        sender.sendMessage("§b/ubp log view <date> [filter] [page] §7- View a specific log file");
    }

    private void listLogs(CommandSender sender) {
        File logDir = new File("plugins/UltimateBackpacks/logs");
        if (!logDir.exists() || !logDir.isDirectory()) {
            sender.sendMessage(ChatColor.RED + "No logs available.");
            return;
        }

        File[] files = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (files == null || files.length == 0) {
            sender.sendMessage(ChatColor.RED + "No logs available.");
            return;
        }

        sender.sendMessage(ChatColor.AQUA + "Available logs:");
        Arrays.stream(files)
                .sorted(Comparator.comparing(File::getName).reversed())
                .forEach(f -> sender.sendMessage(" ▸ " + ChatColor.YELLOW + f.getName().replace(".log", "")));
    }

    private void showLog(CommandSender sender, String date, String filter, int page) {
        File logFile = new File("plugins/UltimateBackpacks/logs/" + date + ".log");
        if (!logFile.exists()) {
            sender.sendMessage(ChatColor.RED + "No log file exists for that date.");
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(logFile.toPath());
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error reading log file.");
            return;
        }

        List<String> filtered = (filter == null)
                ? lines
                : lines.stream().filter(l -> l.toLowerCase().contains(filter.toLowerCase())).collect(Collectors.toList());

        final int linesPerPage = 7;
        int total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / linesPerPage);

        if (total == 0) {
            sender.sendMessage(ChatColor.RED + "No matches found.");
            return;
        }

        if (page < 1 || page > totalPages) {
            sender.sendMessage(ChatColor.RED + "Page out of range. " + ChatColor.YELLOW + totalPages + ChatColor.RED + " pages available.");
            return;
        }

        sender.sendMessage("\n" + ChatColor.DARK_GRAY + "▐ " + ChatColor.GOLD + "Log from " + ChatColor.YELLOW + date + ChatColor.GRAY + " (Page " + ChatColor.AQUA + page + ChatColor.GRAY + " / " + ChatColor.AQUA + totalPages + ChatColor.GRAY + ")\n");

        int start = (page - 1) * linesPerPage;
        int end = Math.min(start + linesPerPage, total);

        for (int i = start; i < end; i++) {
            sendFormattedLogLine(sender, filtered.get(i));
        }

        TextComponent pagination = new TextComponent(ChatColor.GRAY + "Page ");
        pagination.addExtra(ChatColor.AQUA + String.valueOf(page));
        pagination.addExtra(ChatColor.GRAY + "/" + ChatColor.AQUA + totalPages + ChatColor.GRAY + " ");

        for (int i = 1; i <= totalPages; i++) {
            TextComponent number = new TextComponent("[" + i + "]");
            number.setColor(i == page ? net.md_5.bungee.api.ChatColor.YELLOW : net.md_5.bungee.api.ChatColor.GRAY);
            number.setBold(true);
            number.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ubp log view " + date + (filter != null ? " " + filter : "") + " " + i));
            number.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to view page " + i).create()));
            pagination.addExtra(" ");
            pagination.addExtra(number);
        }

        if (sender instanceof Player player) {
            player.spigot().sendMessage(pagination);
        } else {
            sender.sendMessage(pagination.toPlainText());
        }
    }

    private void sendFormattedLogLine(CommandSender sender, String rawLine) {
        String[] parts = rawLine.split("] ", 2);
        if (parts.length < 2) {
            sender.sendMessage(ChatColor.GRAY + rawLine);
            return;
        }

        String timestamp = parts[0].replace("[", "");
        String message = parts[1];
        String timeAgo = getTimeAgo(timestamp);

        TextComponent base = new TextComponent(ChatColor.GRAY + "▸ " + ChatColor.DARK_AQUA + timeAgo + " ");
        TextComponent arrow = new TextComponent(ChatColor.GRAY + "» ");
        TextComponent content = new TextComponent(ChatColor.WHITE + message);
        content.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Exact time: " + ChatColor.YELLOW + timestamp).create()));

        base.addExtra(arrow);
        base.addExtra(content);

        if (sender instanceof Player player) {
            player.spigot().sendMessage(base);
        } else {
            sender.sendMessage("[" + timestamp + "] " + message);
        }
    }

    private String getTimeAgo(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(timestamp);
            Instant instant = date.toInstant();
            Duration duration = Duration.between(instant, Instant.now());

            long seconds = duration.getSeconds();
            if (seconds < 60) return seconds + "s ago";
            long minutes = seconds / 60;
            if (minutes < 60) return minutes + "m ago";
            long hours = minutes / 60;
            if (hours < 24) return hours + "h ago";
            long days = hours / 24;
            return days + "d ago";
        } catch (Exception e) {
            return "unknown time";
        }
    }
}
