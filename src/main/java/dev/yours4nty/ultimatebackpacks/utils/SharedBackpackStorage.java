package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

// Bukkit imports
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// Custom imports
import dev.yours4nty.ultimatebackpacks.storage.SharedBackpackStorageProvider;

public class SharedBackpackStorage {

    private static SharedBackpackStorageProvider provider;

    /**
     * Initializes the SharedBackpackStorage with a specific storage provider.
     *
     * @param storageProvider The storage provider to use for shared backpacks.
     */
    public static void init(SharedBackpackStorageProvider storageProvider) {
        provider = storageProvider;
    }

    /**
     * Gets the current storage provider.
     *
     * @return The current SharedBackpackStorageProvider.
     */
    public static void create(String name, UUID owner) {
        provider.create(name, owner);
    }

    /**
     * Checks if a shared backpack with the given name exists.
     *
     * @param name The name of the shared backpack.
     * @return true if the backpack exists, false otherwise.
     */
    public static boolean exists(String name) {
        return provider.exists(name);
    }

    /**
     * Deletes a shared backpack with the given name.
     *
     * @param name The name of the shared backpack to delete.
     */
    public static void delete(String name) {
        provider.delete(name);
    }

    /**
     * Transfers ownership of a shared backpack to a new owner.
     *
     * @param name The name of the shared backpack.
     * @param newOwner The UUID of the new owner.
     */
    public static void transferOwnership(String name, UUID newOwner) {
        provider.transferOwnership(name, newOwner);
    }

    /**
     * Gets the owner of a shared backpack.
     *
     * @param name The name of the shared backpack.
     * @return The UUID of the owner.
     */
    public static UUID getOwner(String name) {
        return provider.getOwner(name);
    }

    /**
     * Gets the members of a shared backpack.
     *
     * @param name The name of the shared backpack.
     * @return A list of UUIDs representing the members.
     */
    public static List<UUID> getMembers(String name) {
        return provider.getMembers(name);
    }

    /**
     * Adds a member to a shared backpack.
     *
     * @param name The name of the shared backpack.
     * @param uuid The UUID of the member to add.
     */
    public static void addMember(String name, UUID uuid) {
        provider.addMember(name, uuid);
    }

    /**
     * Removes a member from a shared backpack.
     *
     * @param name The name of the shared backpack.
     * @param uuid The UUID of the member to remove.
     */
    public static void removeMember(String name, UUID uuid) {
        provider.removeMember(name, uuid);
    }

    /**
     * Gets a list of all shared backpacks owned by a specific UUID.
     *
     * @param uuid The UUID of the owner.
     * @return A list of shared backpack names.
     */
    public static List<String> getSharedBackpacks(UUID uuid) {
        return provider.getSharedBackpacks(uuid);
    }

    /**
     * Gets the contents of a shared backpack.
     *
     * @param name The name of the shared backpack.
     * @return An array of ItemStacks representing the contents.
     */
    public static ItemStack[] getContents(String name) {
        return provider.getContents(name);
    }

    /**
     * Saves the contents of a shared backpack.
     *
     * @param name The name of the shared backpack.
     * @param contents An array of ItemStacks to save as the contents.
     */
    public static void saveContents(String name, ItemStack[] contents) {
        provider.saveContents(name, contents);
    }

    /**
     * Shuts down the storage provider, releasing any resources it holds.
     */
    public static void shutdown() {
        provider.shutdown();
    }

    // Additional utility methods
    public static List<String> getSharedBackpacksAsMember(UUID playerUUID) {
        File folder = new File("plugins/UltimateBackpacks/sharedBackpacks");
        if (!folder.exists() || !folder.isDirectory()) return Collections.emptyList();

        List<String> result = new ArrayList<>();

        for (File file : Objects.requireNonNull(folder.listFiles((dir, name) -> name.endsWith(".yml")))) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String owner = config.getString("owner");
            List<String> members = config.getStringList("members");

            if (!playerUUID.toString().equals(owner) && members.contains(playerUUID.toString())) {
                result.add(file.getName().replace(".yml", ""));
            }
        }

        return result;
    }

}
