package dev.yours4nty.ultimatebackpacks.storage;

// Java imports
import java.util.UUID;

// Bukkit imports
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface BackpackStorageProvider {

    /**
     * Loads a backpack for the given player and index.
     * @param playerUUID UUID of the player
     * @param index Index of the backpack
     * @return The loaded inventory or null if not found
     */
    Inventory loadBackpack(UUID playerUUID, int index);

    /**
     * Saves the contents of the backpack.
     * @param playerUUID UUID of the player
     * @param index Index of the backpack
     * @param inventory Inventory contents to save
     */
    void saveBackpack(UUID playerUUID, int index, Inventory inventory);

    /**
     * Gets the max amount of backpacks a player can have.
     * This might differ depending on storage (for example, MySQL can limit dynamically).
     * @param player The player
     * @return The max amount of backpacks available
     */
    int getMaxBackpacks(Player player);

    /**
     * Optional cleanup method (if storage backend requires it).
     */
    default void shutdown() {
        // Optional for database disconnect, etc.
    }
    default void init() {
        // Optional init method
    }
}

