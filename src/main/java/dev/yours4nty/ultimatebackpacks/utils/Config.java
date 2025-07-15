package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Bukkit imports
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static String language;
    public static boolean allowNBT;
    public static boolean allowBackpackInsideBackpack;
    public static boolean allowSharedBackpacks;
    public static boolean debugMode;
    public static String storageType;

    private static FileConfiguration rawConfig;

    public static Set<String> blacklistedWorlds = new HashSet<>();
    public static List<String> shulkerTopColors;
    public static List<String> shulkerBottomColors;

    /**
     * Loads the configuration from the provided FileConfiguration.
     *
     * @param config The FileConfiguration to load settings from.
     */
    public static void load(FileConfiguration config) {
        rawConfig = config;
        language = config.getString("language", "en");
        allowNBT = config.getBoolean("allowNBT", true);
        allowBackpackInsideBackpack = config.getBoolean("allowBackpackInsideBackpack", false);
        allowSharedBackpacks = config.getBoolean("allowSharedBackpacks", true);
        debugMode = config.getBoolean("debugMode", false);

        // Storage Settings
        storageType = config.getString("storage", "YAML").toUpperCase();

        // Blacklisted worlds
        blacklistedWorlds.clear();
        blacklistedWorlds.addAll(config.getStringList("WorldSettings.BlacklistWorlds"));

        // Shulker colors
        ConfigurationSection shulkerSection = config.getConfigurationSection("gui.shulkerColors");
        if (shulkerSection != null) {
            shulkerTopColors = shulkerSection.getStringList("top");
            shulkerBottomColors = shulkerSection.getStringList("bottom");
        }
    }
    public static String get(String path) {
        return rawConfig.getString(path);
    }
}
