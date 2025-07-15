package dev.yours4nty.ultimatebackpacks;

// Java Imports
import java.util.List;

// Bukkit API Imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

// Custom Import statements
import dev.yours4nty.ultimatebackpacks.utils.SharedBackpackManager;
import dev.yours4nty.ultimatebackpacks.utils.BackpackStorage;
import dev.yours4nty.ultimatebackpacks.utils.SharedBackpackStorage;
import dev.yours4nty.ultimatebackpacks.commands.BackpackCommand;
import dev.yours4nty.ultimatebackpacks.utils.ViewerContext;
import dev.yours4nty.ultimatebackpacks.utils.Config;
import dev.yours4nty.ultimatebackpacks.utils.WorldUtils;
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;

public class BackpackGUI {

    public static void openSelector(Player player) {
        // Verify if the world is blacklisted
        if (WorldUtils.isBlacklisted(player.getWorld())) {
            player.sendMessage(MessageHandler.get("disabled-in-world"));
            return;
        }

        int unlocked = getBackpackLimit(player);
        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("your-backpacks")
                        .replace("%count%", String.valueOf(unlocked))));


        // Fill the inventory with glass panes
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createGlass());
            gui.setItem(45 + i, createGlass());
        }

        // Fill the sides with glass panes
        for (int i = 9; i < 18; i++) gui.setItem(i, createGlass());
        for (int i = 36; i < 45; i++) gui.setItem(i, createGlass());

        // fill the bottom row with glass panes
        gui.setItem(18, createGlass());

        // Setting the Shulker Boxes for each backpack slot
        List<String> topColors = Config.shulkerTopColors;
        for (int i = 0; i < topColors.size(); i++) {
            try {
                Material shulker = Material.valueOf(topColors.get(i) + "_SHULKER_BOX");
                gui.setItem(19 + i, createBackpackItem(player, i + 1, shulker));
            } catch (IllegalArgumentException e) {
                System.out.println("[UltimateBackpacks] Invalid top shulker color: " + topColors.get(i));
            }
        }

        // Fill the rest of the first two rows with glass panes
        gui.setItem(26, createGlass());
        gui.setItem(27, createGlass());
        gui.setItem(28, createGlass());
        gui.setItem(29, createGlass());

        // Setting the Shulker Boxes for the bottom row
        List<String> bottomColors = Config.shulkerBottomColors;
        for (int i = 0; i < bottomColors.size(); i++) {
            try {
                Material shulker = Material.valueOf(bottomColors.get(i) + "_SHULKER_BOX");
                gui.setItem(30 + i, createBackpackItem(player, 8 + i, shulker));
            } catch (IllegalArgumentException e) {
                System.out.println("[UltimateBackpacks] Invalid bottom shulker color: " + bottomColors.get(i));
            }
        }

        // Fill the rest of the last row with glass panes
        gui.setItem(33, createGlass());
        gui.setItem(34, createGlass());
        gui.setItem(35, createGlass());

        // Setting the shared backpack item if enabled
        if (Config.allowSharedBackpacks) {
            ItemStack shared = new ItemStack(Material.ENDER_CHEST);
            ItemMeta meta = shared.getItemMeta();
            meta.setDisplayName(MessageHandler.get("shared-title"));
            shared.setItemMeta(meta);
            gui.setItem(40, shared);
        }

        // Open the inventory for the player
        player.openInventory(gui);
    }

    public static void openSelectorX(Player viewer, OfflinePlayer target) {
        // Limit the number of backpacks to 10
        int maxBackpacks = 10;

        Inventory gui = Bukkit.createInventory(null, 6 * 9,
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("viewing-backpacks-of")
                        .replace("%player%", target.getName())));

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        // Ordeing the backpacks in a specific pattern
        int[] slotIndices = {
                10, 11, 12, 13, 14, 15, 16,
                28, 29, 30
        };

        // Setting the shulker boxes for each backpack slot
        for (int i = 0; i < maxBackpacks; i++) {
            int slot = slotIndices[i];
            ItemStack shulker = new ItemStack(Material.ORANGE_SHULKER_BOX);
            ItemMeta meta = shulker.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    MessageHandler.get("backpack-name").replace("%number%", String.valueOf(i + 1))));
            shulker.setItemMeta(meta);

            gui.setItem(slot, shulker);
        }

        // Open the inventory for the viewer
        viewer.openInventory(gui);
        ViewerContext.set(viewer.getUniqueId(), target.getUniqueId());
    }

    private static int getBackpackLimit(Player player) {
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission("ultimatebackpacks.limit." + i)) return i;
        }
        return 0;
    }

    private static ItemStack createGlass() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createBackpackItem(Player player, int number, Material shulkerMaterial) {
        int unlocked = getBackpackLimit(player);
        boolean hasAccess = number <= unlocked;

        // If the player doesn't have access to this backpack, use a barrier
        ItemStack item = new ItemStack(hasAccess ? shulkerMaterial : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                MessageHandler.get("backpack-name").replace("%number%", String.valueOf(number))));

        if (!hasAccess) {
            meta.setLore(java.util.Collections.singletonList(
                    ChatColor.translateAlternateColorCodes('&', MessageHandler.get("locked-backpack"))));
        }

        item.setItemMeta(meta);
        return item;
    }

    public static void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (WorldUtils.isBlacklisted(player.getWorld())) {
            player.sendMessage(MessageHandler.get("disabled-in-world"));
            return;
        }

        String expectedTitle = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("your-backpacks").replace("%count%", ""))
        ).split("\\(")[0].trim(); // Ignore the count in the title

        // Check if the clicked inventory is the player's backpack selector
        if (!ChatColor.stripColor(event.getView().getTitle()).startsWith(expectedTitle)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (displayName.equals(ChatColor.stripColor(MessageHandler.get("shared-title")))) {
            if (!Config.allowSharedBackpacks) {
                player.sendMessage(MessageHandler.get("shared-disabled"));
                return;
            }
            SharedBackpackManager.openSharedBackpackSelector(player);
            return;
        }

        // displayName should be in the format "Backpack #1", "Backpack #2", etc
        for (int i = 1; i <= 10; i++) {
            String expectedName = ChatColor.stripColor(
                    ChatColor.translateAlternateColorCodes('&', MessageHandler.get("backpack-name").replace("%number%", String.valueOf(i)))
            );
            if (displayName.equals(expectedName)) {
                if (i > getBackpackLimit(player)) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            MessageHandler.get("cannot-open-backpack").replace("%number%", String.valueOf(i))
                    ));
                    return;
                }
                BackpackStorage.openBackpack(player, i);
                return;
            }
        }


    }

    public static void openSharedSelector(Player player) {
        List<String> sharedNames = SharedBackpackStorage.getSharedBackpacks(player.getUniqueId());

        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("shared-title")));

        // if there are no shared backpacks, show a barrier item
        if (sharedNames.isEmpty()) {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta meta = barrier.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', MessageHandler.get("no-shared-backpacks")));
            barrier.setItemMeta(meta);

            // Pattern for the barrier items
            int[] slots = {21, 22, 23, 30, 31, 32};
            for (int i : slots) {
                gui.setItem(i, barrier);
            }
        } else {
            int index = 1;
            // Pattern for the shared backpack items
            int[] slots = {21, 22, 23, 30, 31, 32};
            for (int i = 0; i < Math.min(sharedNames.size(), 6); i++) {
                String name = sharedNames.get(i);
                ItemStack paper = new ItemStack(Material.BOOK);
                ItemMeta meta = paper.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        MessageHandler.get("shared-backpack-name").replace("%number%", String.valueOf(index))));
                meta.setLore(List.of(ChatColor.translateAlternateColorCodes('&',
                        MessageHandler.get("shared-backpack-lore").replace("%name%", name))));
                paper.setItemMeta(meta);

                gui.setItem(slots[i], paper);
                index++;
            }
        }

        player.openInventory(gui);
    }

}