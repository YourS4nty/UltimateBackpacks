package dev.yours4nty.ultimatebackpacks.commands;

// Bukkit imports
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Custom imports
import dev.yours4nty.ultimatebackpacks.utils.BackpackStorage;
import dev.yours4nty.ultimatebackpacks.BackpackGUI;
import dev.yours4nty.ultimatebackpacks.utils.WorldUtils;
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;

public class OpenBPCommand implements CommandExecutor {

    /**
     * Handles the /openbp command to open a player's backpack.
     *
     * @param sender The command sender, expected to be a Player.
     * @param command The command being executed.
     * @param label The alias used for the command.
     * @param args The arguments passed with the command.
     * @return true if the command was processed successfully, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageHandler.get("ingame-only"));
            return true;
        }

        if (WorldUtils.isBlacklisted(player.getWorld())) {
            player.sendMessage(MessageHandler.get("disabled-in-world"));
            return true;
        }

        int backpackIndex = 1;

        if (args.length > 0) {
            try {
                backpackIndex = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageHandler.get("usage-openbp"));
                return true;
            }

            if (backpackIndex < 1 || backpackIndex > 10) {
                player.sendMessage(MessageHandler.get("backpack-number-out-of-range"));
                return true;
            }
        }

        int limit = getBackpackLimit(player);
        if (backpackIndex > limit) {
            player.sendMessage(MessageHandler.get("no-permission-backpack"));
            return true;
        }

        BackpackGUI.openSelector(player);
        return true;
    }

    /**
     * Determines the maximum number of backpacks a player can have based on their permissions.
     *
     * @param player The player whose backpack limit is being checked.
     * @return The maximum number of backpacks the player can have.
     */
    private int getBackpackLimit(Player player) {
        for (int i = 10; i >= 0; i--) {
            if (player.hasPermission("ultimatebackpacks.limit." + i)) {
                return i;
            }
        }
        return 0;
    }
}
