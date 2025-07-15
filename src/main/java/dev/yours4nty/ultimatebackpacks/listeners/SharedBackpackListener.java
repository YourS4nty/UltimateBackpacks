package dev.yours4nty.ultimatebackpacks.listeners;

// Java imports
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

// Custom imports
import dev.yours4nty.ultimatebackpacks.utils.Config;
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;
import dev.yours4nty.ultimatebackpacks.utils.SharedBackpackManager;
import dev.yours4nty.ultimatebackpacks.utils.SharedBackpackStorage;
import dev.yours4nty.ultimatebackpacks.utils.ActionLogger;

public class SharedBackpackListener implements Listener {

    /**
     * Handles clicks in the shared backpack selector GUI.
     * Cancels the click if it's in the shared backpack selector GUI
     * and opens the selected shared backpack if it exists and the player is a member.
     *
     * @param event The InventoryClickEvent triggered by a player clicking in the inventory.
     */
    @EventHandler
    public void onClickSharedBackpack(InventoryClickEvent event) {
        if (!Config.allowSharedBackpacks) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String guiTitle = ChatColor.stripColor(event.getView().getTitle());
        String expectedTitle = ChatColor.stripColor(MessageHandler.get("shared-title"));

        // Cancel click only in shared backpack selector GUI
        if (guiTitle.equals(expectedTitle)) {
            event.setCancelled(true);
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String template = ChatColor.stripColor(MessageHandler.get("shared-slot-name")); // Ej: "Shared Backpack #%name%"
        String regex = "^" + Pattern.quote(template).replace("%name%", "\\E(.+)\\Q") + "$";
        Matcher matcher = Pattern.compile(regex).matcher(displayName);

        if (!matcher.matches()) return;
        String name = matcher.group(1).trim();

        File file = new File("plugins/UltimateBackpacks/sharedBackpacks", name + ".yml");
        if (!file.exists()) {
            player.sendMessage(MessageHandler.get("shared-backpack-does-not-exist"));
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> members = config.getStringList("members");

        if (!members.contains(player.getUniqueId().toString())) {
            player.sendMessage(MessageHandler.get("shared-backpack-not-member"));
            return;
        }

        // ✅ Open unsing cache
        SharedBackpackManager.openSharedBackpack(player, name);
        //Log
        ActionLogger.log("Player " + player.getName() + " opened shared backpack '" + name + "'");
    }

    /**
     * Handles the closing of a shared backpack inventory.
     * Saves the contents of the shared backpack when the inventory is closed.
     *
     * @param event The InventoryCloseEvent triggered when a player closes an inventory.
     */
    @EventHandler
    public void onCloseSharedBackpack(InventoryCloseEvent event) {
        if (!Config.allowSharedBackpacks) return;

        Inventory closed = event.getInventory();
        String name = dev.yours4nty.ultimatebackpacks.utils.SharedBackpackManager.getSharedBackpackNameFromInventory(closed);
        if (name == null) return;

        // Verify inventory is still open?
        boolean stillOpen = false;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getOpenInventory().getTopInventory().equals(closed)) {
                stillOpen = true;
                break;
            }
        }

        if (!stillOpen) {
            // ✅ Save the content And clean all caché
            SharedBackpackStorage.saveContents(name, closed.getContents());
            dev.yours4nty.ultimatebackpacks.utils.SharedBackpackManager.removeCachedInventory(name);
            //Log
            ActionLogger.log("Player " + event.getPlayer().getName() + " closed shared backpack '" + name + "'");
        }
    }
}
