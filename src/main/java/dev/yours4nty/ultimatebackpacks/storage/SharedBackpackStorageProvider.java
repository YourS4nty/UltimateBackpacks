package dev.yours4nty.ultimatebackpacks.storage;

// Java imports
import java.util.List;
import java.util.UUID;

// Bukkit imports
import org.bukkit.inventory.ItemStack;

/**
 * Interface for managing shared backpack storage.
 * Provides methods to create, delete, and manage shared backpacks,
 * including ownership and membership functionalities.
 */
public interface SharedBackpackStorageProvider {

    void create(String name, UUID owner);

    boolean exists(String name);

    void delete(String name);

    void transferOwnership(String name, UUID newOwner);

    UUID getOwner(String name);

    List<UUID> getMembers(String name);

    void addMember(String name, UUID uuid);

    void removeMember(String name, UUID uuid);

    List<String> getSharedBackpacks(UUID member);

    ItemStack[] getContents(String name);

    void saveContents(String name, ItemStack[] contents);

    default void shutdown() {}
}
