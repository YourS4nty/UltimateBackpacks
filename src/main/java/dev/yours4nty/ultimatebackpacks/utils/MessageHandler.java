package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.util.HashMap;
import java.util.Map;
import java.io.File;

// Bukkit imports
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// Custom imports
import dev.yours4nty.ultimatebackpacks.UltimateBackpacks;

public class MessageHandler {

    // A map to hold the messages loaded from the configuration file
    private static final Map<String, String> messages = new HashMap<>();
    private static String selectedLang = "en";

    // Method to load messages from the messages.yml file
    public static void loadMessages() {
        File file = new File(UltimateBackpacks.getInstance().getDataFolder(), "messages.yml");
        FileConfiguration messagesFile = YamlConfiguration.loadConfiguration(file);

        selectedLang = Config.language;
        messages.clear();

        // Check if the selected language section exists
        String langPath = "languages." + selectedLang;
        if (!messagesFile.isConfigurationSection(langPath)) {
            UltimateBackpacks.getInstance().getLogger().warning("[UltimateBackpacks] Language section not found: " + langPath);
            return;
        }

        // Load messages for the selected language
        for (String key : messagesFile.getConfigurationSection(langPath).getKeys(false)) {
            String value = messagesFile.getString(langPath + "." + key);
            if (value != null) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', value));
            }
        }

        UltimateBackpacks.getInstance().getLogger().info("[UltimateBackpacks] Loaded language: " + selectedLang);
    }

    // Method to get a message by key
    public static String get(String key) {
        return messages.getOrDefault(key, ChatColor.RED + "Missing message: " + key);
    }

    public static String get(String key, Map<String, String> placeholders) {
        String message = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }
}
