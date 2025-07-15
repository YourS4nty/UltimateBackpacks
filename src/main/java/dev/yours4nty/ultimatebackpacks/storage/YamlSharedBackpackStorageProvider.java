package dev.yours4nty.ultimatebackpacks.storage;

// Java imports
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// Bukkit imports
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

// Custom imports
import dev.yours4nty.ultimatebackpacks.UltimateBackpacks;

public class YamlSharedBackpackStorageProvider implements SharedBackpackStorageProvider {

    private final File folder;

    /**
     * Constructor for the YamlSharedBackpackStorageProvider.
     * Initializes the storage folder for shared backpacks.
     */
    public YamlSharedBackpackStorageProvider() {
        this.folder = new File(UltimateBackpacks.getInstance().getDataFolder(), "sharedBackpacks");
        if (!folder.exists()) folder.mkdirs();
    }

    /**
     * Gets the file for a given backpack name.
     * The file is named in lowercase with a .yml extension.
     *
     * @param name The name of the backpack.
     * @return The File object representing the backpack's storage file.
     */
    private File getFile(String name) {
        return new File(folder, name.toLowerCase() + ".yml");
    }

    /**
     * Loads the configuration from a YAML file.
     *
     * @param name The name of the backpack.
     * @return The FileConfiguration loaded from the YAML file.
     */
    private FileConfiguration load(String name) {
        return YamlConfiguration.loadConfiguration(getFile(name));
    }

    /**
     * Saves the configuration to a YAML file.
     *
     * @param name   The name of the backpack.
     * @param config The FileConfiguration to save.
     */
    private void save(String name, FileConfiguration config) {
        try {
            config.save(getFile(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Override methods from SharedBackpackStorageProvider

    @Override
    public void create(String name, UUID owner) {
        if (exists(name)) return;
        FileConfiguration config = new YamlConfiguration();
        config.set("owner", owner.toString());
        config.set("members", Collections.singletonList(owner.toString()));
        config.set("contents", new ItemStack[54]);
        save(name, config);
    }

    @Override
    public boolean exists(String name) {
        return getFile(name).exists();
    }

    @Override
    public void delete(String name) {
        File file = getFile(name);
        if (file.exists()) file.delete();
    }

    @Override
    public void transferOwnership(String name, UUID newOwner) {
        FileConfiguration config = load(name);
        config.set("owner", newOwner.toString());
        save(name, config);
    }

    @Override
    public UUID getOwner(String name) {
        String id = load(name).getString("owner");
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<UUID> getMembers(String name) {
        return load(name).getStringList("members")
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    @Override
    public void addMember(String name, UUID uuid) {
        FileConfiguration config = load(name);
        List<String> members = config.getStringList("members");
        if (!members.contains(uuid.toString())) {
            members.add(uuid.toString());
            config.set("members", members);
            save(name, config);
        }
    }

    @Override
    public void removeMember(String name, UUID uuid) {
        FileConfiguration config = load(name);
        List<String> members = config.getStringList("members");
        members.remove(uuid.toString());
        config.set("members", members);
        save(name, config);
    }

    @Override
    public List<String> getSharedBackpacks(UUID uuid) {
        File[] files = folder.listFiles();
        if (files == null) return Collections.emptyList();

        return Arrays.stream(files)
                .filter(file -> {
                    FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                    return cfg.getStringList("members").contains(uuid.toString());
                })
                .map(file -> file.getName().replace(".yml", ""))
                .collect(Collectors.toList());
    }

    @Override
    public ItemStack[] getContents(String name) {
        List<?> raw = load(name).getList("contents");
        if (raw == null) return new ItemStack[54];
        return raw.toArray(new ItemStack[54]);
    }

    @Override
    public void saveContents(String name, ItemStack[] contents) {
        FileConfiguration config = load(name);
        config.set("contents", contents);
        save(name, config);
    }
}
