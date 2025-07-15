package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// Custom imports
import dev.yours4nty.ultimatebackpacks.UltimateBackpacks;
import dev.yours4nty.ultimatebackpacks.utils.SharedBackpackStorage;
import dev.yours4nty.ultimatebackpacks.utils.Config;

public class SharedBackpackManager {

    // Directory for shared backpacks
    private static final File folder = new File("plugins/UltimateBackpacks/sharedBackpacks");
    // Open SharedBackpack: Cache
    private static final Map<String, Inventory> openSharedInventories = new HashMap<>();

    static {
        if (!folder.exists()) folder.mkdirs();
    }

    public static boolean createSharedBackpack(String name, Player owner) {
        File file = new File(UltimateBackpacks.getInstance().getDataFolder(), "sharedBackpacks/" + name + ".yml");
        if (file.exists()) return false;

        UUID uuid = owner.getUniqueId();

        // Check if the owner has reached the limit of shared backpacks
        List<String> allShared = getPlayerSharedBackpacks(uuid);
        if (allShared.size() >= 6) {
            owner.sendMessage(MessageHandler.get("shared-limit-reached"));
            return false;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.set("owner", uuid.toString());

        // Adding the owner as a member
        List<String> members = new ArrayList<>();
        members.add(uuid.toString()); // This is for the owner to be included in the members list and open the backpack
        config.set("members", members);

        try {
            config.save(file);
            // Log
            ActionLogger.log("Player " + owner.getName() + " created shared backpack '" + name + "'");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public static boolean addUser(String name, OfflinePlayer user) {
        if (!Config.allowSharedBackpacks) return false;

        List<String> userShared = getPlayerSharedBackpacks(user.getUniqueId());
        if (userShared.size() >= 6) {
            return false; // User has reached the limit of shared backpacks
        }

        File file = new File(folder, name + ".yml");
        if (!file.exists()) return false;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> members = config.getStringList("members");
        String uuid = user.getUniqueId().toString();

        if (members.contains(uuid)) return false;

        members.add(uuid);
        config.set("members", members);

        try {
            config.save(file);
            // Log
            ActionLogger.log("Player " + user.getName() + " was added to shared backpack '" + name + "'");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean removeUser(String name, OfflinePlayer user) {
        if (!Config.allowSharedBackpacks) return false;

        File file = new File(folder, name + ".yml");
        if (!file.exists()) return false;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> members = config.getStringList("members");
        String uuid = user.getUniqueId().toString();

        if (!members.contains(uuid)) return false;

        members.remove(uuid);
        config.set("members", members);

        try {
            config.save(file);
            // Log
            ActionLogger.log("Player " + user.getName() + " was removed from shared backpack '" + name + "'");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteSharedBackpack(String name) {
        if (!Config.allowSharedBackpacks) return false;

        File file = new File(folder, name + ".yml");
        return file.exists() && file.delete();
    }

    public static List<String> getMemberNames(String name) {
        if (!Config.allowSharedBackpacks) return List.of();

        File file = new File(folder, name + ".yml");
        if (!file.exists()) return List.of();

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> ids = config.getStringList("members");

        List<String> names = new ArrayList<>();
        for (String id : ids) {
            try {
                OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(id));
                names.add(op.getName() != null ? op.getName() : "Unknown");
            } catch (IllegalArgumentException ignored) {}
        }
        return names;
    }


    public static boolean transferOwnership(String name, OfflinePlayer newOwner) {
        if (!Config.allowSharedBackpacks) return false;

        File file = new File(folder, name + ".yml");
        if (!file.exists()) return false;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String newOwnerUUID = newOwner.getUniqueId().toString();
        List<String> members = config.getStringList("members");

        if (!members.contains(newOwnerUUID)) members.add(newOwnerUUID);

        config.set("owner", newOwnerUUID);
        config.set("members", members);

        try {
            config.save(file);
            // Log
            ActionLogger.log("Ownership of shared backpack '" + name + "' transferred to " + newOwner.getName());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isOwner(String name, UUID uuid) {
        File file = new File(folder, name + ".yml");
        if (!file.exists()) return false;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        return uuid.toString().equals(config.getString("owner"));
    }

    public static boolean exists(String name) {
        return new File(folder, name + ".yml").exists();
    }

    public static List<String> getPlayerSharedBackpacks(UUID playerUUID) {
        List<String> result = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<String> members = config.getStringList("members");
            if (members.contains(playerUUID.toString())) {
                result.add(file.getName().replace(".yml", ""));
            }
        }
        return result;
    }

    public static UUID getOwner(String name) {
        File file = new File(folder, name + ".yml");
        if (!file.exists()) return null;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        try {
            return UUID.fromString(config.getString("owner"));
        } catch (Exception e) {
            return null;
        }
    }

    public static void openSharedBackpackSelector(Player player) {
        if (!Config.allowSharedBackpacks) {
            player.sendMessage(MessageHandler.get("shared-disabled"));
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 6 * 9,
                ChatColor.translateAlternateColorCodes('&', MessageHandler.get("shared-title")));

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        // Pattern for center slots
        int[] centerSlots = {21, 22, 23, 30, 31, 32};

        List<String> backpacks = SharedBackpackManager.getPlayerSharedBackpacks(player.getUniqueId());

        int shown = 0;
        for (int i = 0; i < Math.min(backpacks.size(), 6); i++) {
            String name = backpacks.get(i);

            UUID ownerUUID = SharedBackpackManager.getOwner(name);
            if (ownerUUID == null) continue;

            List<String> allMembers = SharedBackpackManager.getMemberNames(name);

            ItemStack item = new ItemStack(Material.ENDER_CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    MessageHandler.get("shared-backpack-name").replace("%number%", name)));
            meta.setLore(List.of(ChatColor.translateAlternateColorCodes('&',
                    MessageHandler.get("shared-backpack-lore-members").replace("%list%", String.join(", ", allMembers)))));
            item.setItemMeta(meta);

            gui.setItem(centerSlots[i], item);
            shown++;
        }

        // Fill remaining slots with barriers
        for (int i = shown; i < 6; i++) {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta meta = barrier.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    MessageHandler.get("shared-locked-slot-name")));
            meta.setLore(List.of(ChatColor.translateAlternateColorCodes('&',
                    MessageHandler.get("shared-locked-slot-lore"))));
            barrier.setItemMeta(meta);

            gui.setItem(centerSlots[i], barrier);
        }

        player.openInventory(gui);
    }

    // Storing in the cache
    public static void openSharedBackpack(Player player, String name) {
        Inventory inv;

        if (openSharedInventories.containsKey(name)) {
            inv = openSharedInventories.get(name);
        } else {
            inv = Bukkit.createInventory(null, 54,
                    ChatColor.translateAlternateColorCodes('&',
                            MessageHandler.get("shared-backpack-title").replace("%name%", name)));
            inv.setContents(SharedBackpackStorage.getContents(name));
            openSharedInventories.put(name, inv);
        }

        player.openInventory(inv);
    }

    public static void removeCachedInventory(String name) {
        openSharedInventories.remove(name);
    }

    public static String getSharedBackpackNameFromInventory(Inventory inv) {
        for (Map.Entry<String, Inventory> entry : openSharedInventories.entrySet()) {
            if (entry.getValue().equals(inv)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
