package dev.yours4nty.ultimatebackpacks.gui;

// Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

// Custom imports
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;
import dev.yours4nty.ultimatebackpacks.utils.WorldUtils;

// Extra imports
import net.md_5.bungee.api.ChatColor;

public class SharedBackpackGUI {

    /**
     * Opens the shared backpack selector GUI for the player.
     *
     * @param player The player to open the GUI for.
     */
    public static void openSharedSelector(Player player) {
        if (WorldUtils.isBlacklisted(player.getWorld())) {
            player.sendMessage(MessageHandler.get("disabled-in-world"));
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("shared-title")));

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        int[] centerSlots = {21, 22, 23, 30, 31, 32};
        File folder = new File("plugins/UltimateBackpacks/sharedBackpacks");
        List<File> owned = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (!file.getName().endsWith(".yml")) continue;

                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                List<String> members = config.getStringList("members");
                if (members.contains(player.getUniqueId().toString())) {
                    owned.add(file);
                }
            }
        }

        if (owned.isEmpty()) {
            // if no shared backpacks are found, show empty slots
            for (int slot : centerSlots) {
                ItemStack item = new ItemStack(Material.BARRIER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        MessageHandler.get("no-shared-backpacks")));
                item.setItemMeta(meta);
                gui.setItem(slot, item);
            }
        } else {
            // Show owned shared backpacks in the center slots
            int index = 0;
            for (File file : owned) {
                if (index >= centerSlots.length) break;

                String name = file.getName().replace(".yml", "");
                ItemStack item = new ItemStack(Material.BARRIER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        MessageHandler.get("shared-slot-name").replace("%name%", name)));
                meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&',
                        MessageHandler.get("shared-slot-lore"))));
                item.setItemMeta(meta);

                gui.setItem(centerSlots[index], item);
                index++;
            }
        }

        player.openInventory(gui);
    }
}
