package dev.yours4nty.ultimatebackpacks.storage;

// Java imports
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

// Custom imports
import dev.yours4nty.ultimatebackpacks.UltimateBackpacks;

public class YamlBackpackStorageProvider implements BackpackStorageProvider {

    private final File folder;

    /**
     * Constructor for YamlBackpackStorageProvider.
     * Initializes the folder where backpack data will be stored.
     */
    public YamlBackpackStorageProvider() {
        this.folder = new File(UltimateBackpacks.getInstance().getDataFolder(), "userBackpacks");
        if (!folder.exists()) folder.mkdirs();
    }

    // Override methods from BackpackStorageProvider

    @Override
    public Inventory loadBackpack(UUID playerUUID, int index) {
        File file = new File(folder, playerUUID.toString() + ".yml");
        if (!file.exists()) return null;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "backpack-" + index;

        if (!config.contains(path)) return null;

        Inventory inventory = Bukkit.createInventory(null, 54, "Backpack #" + index);
        List<?> list = config.getList(path);
        if (list != null) {
            ItemStack[] contents = list.toArray(new ItemStack[0]);
            inventory.setContents(contents);
        }

        return inventory;
    }

    @Override
    public void saveBackpack(UUID playerUUID, int index, Inventory inventory) {
        File file = new File(folder, playerUUID.toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<ItemStack> contents = Arrays.asList(inventory.getContents());
        config.set("backpack-" + index, contents);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getMaxBackpacks(Player player) {
        // Aquí puedes implementar lógica por permisos si lo deseas
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission("ultimatebackpacks.limit." + i)) {
                return i;
            }
        }
        return 0;
    }

}

