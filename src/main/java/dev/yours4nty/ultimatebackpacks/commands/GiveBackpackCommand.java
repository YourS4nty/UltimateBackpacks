package dev.yours4nty.ultimatebackpacks.commands;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// Custom imports
import dev.yours4nty.ultimatebackpacks.utils.ItemBuilder;
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;

public class GiveBackpackCommand implements CommandExecutor {

    /**
     * Constructor for the GiveBackpackCommand class.
     * Registers the command executor for the /givebackpack command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimatebackpacks.give")) {
            sender.sendMessage(MessageHandler.get("no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageHandler.get("usage-givebackpack"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(MessageHandler.get("player-not-online").replace("%player%", args[0]));
            return true;
        }

        // Crete a backpack item using the ItemBuilder utility
        ItemStack backpack = ItemBuilder.createBackpackItem();

        target.getInventory().addItem(backpack);
        target.sendMessage(MessageHandler.get("backpack-received"));

        if (!target.equals(sender)) {
            sender.sendMessage(MessageHandler.get("give-success").replace("%player%", target.getName()));
        }

        return true;
    }
}
