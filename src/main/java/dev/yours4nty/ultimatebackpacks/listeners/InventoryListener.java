package dev.yours4nty.ultimatebackpacks.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import dev.yours4nty.ultimatebackpacks.utils.BackpackStorage;
import dev.yours4nty.ultimatebackpacks.BackpackGUI;
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;
import dev.yours4nty.ultimatebackpacks.utils.Config;
import dev.yours4nty.ultimatebackpacks.utils.ActionLogger;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        BackpackGUI.handleClick(event);

        Inventory topInventory = event.getView().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null || topInventory == null) return;
        if (!isBackpackInventory(topInventory)) return;

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        String invTitle = ChatColor.stripColor(event.getView().getTitle());

        // Block backpacks inside backpacks
        if (!Config.allowBackpackInsideBackpack) {
            if (cursor != null && isBackpackItem(cursor)) {
                event.setCancelled(true);
                player.sendMessage(MessageHandler.get("backpack-inside-error"));
                ActionLogger.log(player.getName() + " tried to insert a backpack using cursor into: '" + invTitle + "'");
                return;
            }

            if (currentItem != null && isBackpackItem(currentItem)) {
                event.setCancelled(true);
                player.sendMessage(MessageHandler.get("backpack-inside-error-current"));
                ActionLogger.log(player.getName() + " tried to insert a backpack using current item into: '" + invTitle + "'");
                return;
            }

            if (event.getClick().isKeyboardClick() && event.getHotbarButton() >= 0) {
                ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                if (isBackpackItem(hotbarItem)) {
                    event.setCancelled(true);
                    player.sendMessage(MessageHandler.get("backpack-inside-error-current"));
                    ActionLogger.log(player.getName() + " tried hotbar-swap with a backpack into: '" + invTitle + "'");
                    return;
                }
            }

            if (event.getAction().toString().contains("MOVE_TO_OTHER") && isBackpackItem(currentItem)) {
                if (clickedInventory.equals(player.getInventory())) {
                    event.setCancelled(true);
                    player.sendMessage(MessageHandler.get("backpack-inside-error"));
                    ActionLogger.log(player.getName() + " tried shift-clicking a backpack into: '" + invTitle + "'");
                    return;
                }
            }
        }

        // Detailed logging of item actions
        String action = event.getAction().toString();
        String clickType = event.getClick().toString();
        int slot = event.getSlot();

        if (currentItem != null && currentItem.getType() != Material.AIR) {
            String itemName = currentItem.getType().name();
            int amount = currentItem.getAmount();
            ActionLogger.log("ItemClick by " + player.getName()
                    + " in '" + invTitle + "'"
                    + " | Slot: " + slot
                    + " | Click: " + clickType
                    + " | Action: " + action
                    + " | Item: " + itemName + " x" + amount);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory closedInventory = event.getInventory();
        String title = event.getView().getTitle();

        String localizedPrefix = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("backpack-inventory-title").replace("%number%", ""))
        ).toLowerCase();

        if (title != null && ChatColor.stripColor(title).toLowerCase().startsWith(localizedPrefix)) {
            try {
                String numberStr = ChatColor.stripColor(title).replaceAll("[^0-9]", "");
                int index = Integer.parseInt(numberStr.trim());
                if (index >= 1 && index <= 10) {
                    BackpackStorage.saveBackpack(player, index);
                    ActionLogger.log(player.getName() + " closed and saved Backpack #" + index);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String invTitle = ChatColor.stripColor(event.getView().getTitle());
        if (!invTitle.toLowerCase().contains("backpack")) return;

        ItemStack item = event.getOldCursor();
        if (item == null || item.getType() == Material.AIR) return;

        ActionLogger.log("Drag by " + player.getName()
                + " of " + item.getType() + " x" + item.getAmount()
                + " across slots " + event.getRawSlots()
                + " in '" + invTitle + "'");
    }

    private boolean isBackpackItem(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) return false;

        String template = MessageHandler.get("backpack-item-name");
        String expectedPrefix = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', template)).toLowerCase();

        String displayName = item.getItemMeta().getDisplayName();
        if (displayName == null) return false;

        String strippedDisplayName = ChatColor.stripColor(displayName).toLowerCase();
        return strippedDisplayName.startsWith(expectedPrefix);
    }

    private boolean isBackpackInventory(Inventory inv) {
        if (inv == null || inv.getViewers().isEmpty()) return false;

        Player viewer = (Player) inv.getViewers().get(0);
        String title = ChatColor.stripColor(viewer.getOpenInventory().getTitle()).toLowerCase();

        String personal = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("backpack-inventory-title").replace("%number%", ""))
        ).toLowerCase();

        String shared = ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("shared-backpack-title").replace("%name%", ""))
        ).toLowerCase();

        return title.startsWith(personal) || title.startsWith(shared);
    }
}
