package dev.yours4nty.ultimatebackpacks.commands;

// Java imports
import java.io.File;
import java.io.IOException;
import java.util.List;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// Custom imports
import dev.yours4nty.ultimatebackpacks.utils.SharedBackpackManager;
import dev.yours4nty.ultimatebackpacks.utils.WorldUtils;
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;
import dev.yours4nty.ultimatebackpacks.utils.Config;
import dev.yours4nty.ultimatebackpacks.utils.ActionLogger;

public class BackpackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageHandler.get("ingame-only"));
            return true;
        }

        if (!Config.allowSharedBackpacks) {
            player.sendMessage(MessageHandler.get("shared-disabled"));
            return true;
        }

        if (WorldUtils.isBlacklisted(player.getWorld())) {
            player.sendMessage(MessageHandler.get("disabled-in-world"));
            return true;
        }

        if (args.length < 2) {
            sendUsage(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        String name = args[1];

        // Handling all backpack commands
        switch (sub) {
            case "newshared" -> {
                if (!isValidName(name)) {
                    player.sendMessage(MessageHandler.get("invalid-shared-name"));
                    return true;
                }

                if (SharedBackpackManager.exists(name)) {
                    player.sendMessage(MessageHandler.get("shared-already-exists"));
                    return true;
                }

                if (SharedBackpackManager.createSharedBackpack(name, player)) {
                    player.sendMessage(MessageHandler.get("shared-created").replace("%name%", name));
                } else {
                    player.sendMessage(MessageHandler.get("shared-creation-failed"));
                }
            }

            case "adduser" -> {
                if (args.length < 3) {
                    player.sendMessage(MessageHandler.get("usage-adduser"));
                    return true;
                }

                if (!SharedBackpackManager.isOwner(name, player.getUniqueId())) {
                    player.sendMessage(MessageHandler.get("not-owner"));
                    return true;
                }

                OfflinePlayer toAdd = Bukkit.getOfflinePlayer(args[2]);
                if (SharedBackpackManager.addUser(name, toAdd)) {
                    player.sendMessage(MessageHandler.get("user-added").replace("%name%", name));
                } else {
                    player.sendMessage(MessageHandler.get("user-add-failed"));
                }
            }

            case "removeuser" -> {
                if (args.length < 3) {
                    player.sendMessage(MessageHandler.get("usage-removeuser"));
                    return true;
                }

                if (!SharedBackpackManager.isOwner(name, player.getUniqueId())) {
                    player.sendMessage(MessageHandler.get("not-owner"));
                    return true;
                }

                OfflinePlayer toRemove = Bukkit.getOfflinePlayer(args[2]);
                if (SharedBackpackManager.removeUser(name, toRemove)) {
                    player.sendMessage(MessageHandler.get("user-removed").replace("%name%", name));
                } else {
                    player.sendMessage(MessageHandler.get("user-remove-failed"));
                }
            }

            case "delshared" -> {
                if (!SharedBackpackManager.isOwner(name, player.getUniqueId())) {
                    player.sendMessage(MessageHandler.get("not-owner"));
                    return true;
                }

                if (SharedBackpackManager.deleteSharedBackpack(name)) {
                    player.sendMessage(MessageHandler.get("shared-deleted"));
                    // Log
                    ActionLogger.log("Shared backpack '" + name + "' was deleted.");
                } else {
                    player.sendMessage(MessageHandler.get("shared-deletion-failed"));
                }
            }

            case "transferowner" -> {
                if (args.length < 3) {
                    player.sendMessage(MessageHandler.get("usage-transferowner"));
                    return true;
                }

                if (!SharedBackpackManager.isOwner(name, player.getUniqueId())) {
                    player.sendMessage(MessageHandler.get("not-owner"));
                    return true;
                }

                OfflinePlayer newOwner = Bukkit.getOfflinePlayer(args[2]);
                if (SharedBackpackManager.transferOwnership(name, newOwner)) {
                    player.sendMessage(MessageHandler.get("ownership-transferred"));
                } else {
                    player.sendMessage(MessageHandler.get("transfer-failed"));
                }
            }

            case "leave" -> {
                File file = new File("plugins/UltimateBackpacks/sharedBackpacks", name + ".yml");

                if (!file.exists()) {
                    player.sendMessage(MessageHandler.get("shared-backpack-does-not-exist"));
                    return true;
                }

                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String ownerUUID = config.getString("owner");
                List<String> members = config.getStringList("members");

                if (ownerUUID != null && ownerUUID.equals(player.getUniqueId().toString())) {
                    player.sendMessage(MessageHandler.get("cannot-leave-own-backpack"));
                    return true;
                }

                if (!members.contains(player.getUniqueId().toString())) {
                    player.sendMessage(MessageHandler.get("shared-backpack-not-member"));
                    return true;
                }

                members.remove(player.getUniqueId().toString());
                config.set("members", members);

                try {
                    config.save(file);
                    player.sendMessage(MessageHandler.get("left-shared-backpack").replace("%name%", name));
                } catch (IOException e) {
                    player.sendMessage(MessageHandler.get("error-leaving-backpack"));
                    e.printStackTrace();
                }
            }

            // Unrecognized subcommand
            default -> player.sendMessage(MessageHandler.get("unknown-subcommand"));
        }

        return true;
    }

    // Sends usage instructions to the player
    private void sendUsage(Player player) {
        player.sendMessage(MessageHandler.get("backpack-usage-header"));
        player.sendMessage(MessageHandler.get("backpack-usage-line1"));
        player.sendMessage(MessageHandler.get("backpack-usage-line2"));
        player.sendMessage(MessageHandler.get("backpack-usage-line3"));
        player.sendMessage(MessageHandler.get("backpack-usage-line4"));
        player.sendMessage(MessageHandler.get("backpack-usage-line5"));
    }

    private boolean isValidName(String name) {
        return name.matches("^[a-zA-Z0-9_-]{3,20}$");
    }
}
