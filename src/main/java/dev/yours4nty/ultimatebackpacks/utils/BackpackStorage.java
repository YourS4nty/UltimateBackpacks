package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.util.*;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

// Custom imports
import dev.yours4nty.ultimatebackpacks.UltimateBackpacks;
import dev.yours4nty.ultimatebackpacks.storage.BackpackStorageProvider;
import dev.yours4nty.ultimatebackpacks.utils.ActionLogger;

public class BackpackStorage {

    private static final Map<UUID, Map<Integer, Inventory>> backpacks = new HashMap<>();
    private static BackpackStorageProvider provider;

    // Private constructor to prevent instantiation
    public static void init(BackpackStorageProvider storageProvider) {
        provider = storageProvider;
    }

    /**
     * Retrieves the backpack inventory for a player at a specific index.
     * If the backpack does not exist, it creates a new one and loads its contents.
     *
     * @param uuid  The UUID of the player
     * @param index The index of the backpack
     * @return The Inventory object representing the player's backpack
     */
    public static Inventory getBackpack(UUID uuid, int index) {
        backpacks.putIfAbsent(uuid, new HashMap<>());
        Map<Integer, Inventory> playerBackpacks = backpacks.get(uuid);

        if (!playerBackpacks.containsKey(index)) {
            String title = ChatColor.translateAlternateColorCodes('&',
                    MessageHandler.get("backpack-inventory-title").replace("%number%", String.valueOf(index))
            );

            Inventory inv = Bukkit.createInventory(null, 54, title);
            Inventory loaded = provider.loadBackpack(uuid, index);
            if (loaded != null) {
                inv.setContents(loaded.getContents());
            }

            playerBackpacks.put(index, inv);
        }

        return playerBackpacks.get(index);
    }

    /**
     * Retrieves the backpack inventory for a player at a specific index.
     * If the backpack does not exist, it creates a new one.
     *
     * @param player The player whose backpack is being accessed
     * @param index  The index of the backpack
     * @return The Inventory object representing the player's backpack
     */
    public static void saveBackpack(Player player, int index) {
        UUID uuid = player.getUniqueId();
        Inventory inv = getBackpack(uuid, index);
        provider.saveBackpack(uuid, index, inv);
        // Log
        ActionLogger.log("Player " + player.getName() + " closed personal backpack #" + index);
    }

    /**
     * Saves all backpacks for a player.
     *
     * @param uuid The UUID of the player whose backpacks are being saved
     */
    public static void saveAllBackpacks(UUID uuid) {
        Map<Integer, Inventory> playerBackpacks = backpacks.get(uuid);
        if (playerBackpacks == null) return;

        for (Map.Entry<Integer, Inventory> entry : playerBackpacks.entrySet()) {
            int index = entry.getKey();
            provider.saveBackpack(uuid, index, entry.getValue());
        }
    }

    /**
     * Opens a player's backpack inventory.
     *
     * @param player The player whose backpack is being opened
     * @param index  The index of the backpack to open
     */
    public static void openBackpack(Player player, int index) {
        Inventory inv = getBackpack(player.getUniqueId(), index);
        player.openInventory(inv);
        // Log
        ActionLogger.log("Player " + player.getName() + " opened personal backpack #" + index);
    }

    /**
     * Opens a backpack for a specific viewer.
     *
     * @param viewer      The player who is viewing the backpack
     * @param targetUUID  The UUID of the player whose backpack is being viewed
     * @param index       The index of the backpack to open
     */
    public static void openBackpack(Player viewer, UUID targetUUID, int index) {
        Inventory inv = getBackpack(targetUUID, index);
        viewer.openInventory(inv);
        // Log
        ActionLogger.log("Player " + viewer.getName() + " opened personal backpack #" + index);
    }

    /**
     * Gets the maximum number of backpacks a player can have.
     *
     * @param player The player whose maximum backpacks are being queried
     * @return The maximum number of backpacks the player can have
     */
    public static int getMaxBackpacks(Player player) {
        return provider.getMaxBackpacks(player);
    }
}
