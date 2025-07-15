package dev.yours4nty.ultimatebackpacks;

// Default Imports
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

// Custom Imports (Commands)
import dev.yours4nty.ultimatebackpacks.commands.*;
import dev.yours4nty.ultimatebackpacks.listeners.*;
import dev.yours4nty.ultimatebackpacks.storage.*;
import dev.yours4nty.ultimatebackpacks.utils.*;

public class UltimateBackpacks extends JavaPlugin {

    private static UltimateBackpacks instance;
    private static BackpackStorageProvider storageProvider;

    @Override
    public void onEnable() {
        instance = this;

        // Default Settings
        saveDefaultConfig();
        Config.load(getConfig());
        getDataFolder().mkdirs();
        WorldUtils.loadWorldBlacklist();

        // Messages
        saveResource("messages.yml", false);
        MessageHandler.loadMessages();
        String lang = getConfig().getString("language", "en");

        // ==== STORAGE SYSTEM ====
        String type = getConfig().getString("storage.type", "yaml").toLowerCase();
        switch (type) {
            case "mysql" -> {
                storageProvider = new MySQLBackpackStorageProvider();
                ((MySQLBackpackStorageProvider) storageProvider).init();
            }
            case "sqlite" -> {
                storageProvider = new SQLiteBackpackStorageProvider();
                ((SQLiteBackpackStorageProvider) storageProvider).init();
            }
            default -> {
                storageProvider = new YamlBackpackStorageProvider();
                ((YamlBackpackStorageProvider) storageProvider).init();
            }
        }

        // Initialization for backward compatibility
        BackpackStorage.init(storageProvider);
        SharedBackpackStorage.init(new YamlSharedBackpackStorageProvider());

        // Commands
        getCommand("openbp").setExecutor(new OpenBPCommand());
        getCommand("givebackpack").setExecutor(new GiveBackpackCommand());
        getCommand("ubp").setExecutor(new AdminBackpackCommand(this));
        getCommand("ubp").setTabCompleter(new AdminBackpackTabCompleter());
        getCommand("backpack").setExecutor(new BackpackCommand());
        getCommand("backpack").setTabCompleter(new BackpackTabCompleter());

        // Events
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BackpackItemListener(), this);
        getServer().getPluginManager().registerEvents(new SharedBackpackListener(), this);

        // Log
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §5╔════════════════════════════════════════╗");
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §d  UltimateBackpacks activado correctamente!");
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §7  Versión: §e1.0§7 | Autor: §bYourS4nty");
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §7  Idioma cargado: §a" + lang);
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §7  Modo de almacenamiento: §e" + type.toUpperCase());
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §5╚════════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (storageProvider != null) {
            storageProvider.shutdown();
        }
        // Log
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §5╔════════════════════════════════════════╗");
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §c  UltimateBackpacks ha sido desactivado §7:(");
        Bukkit.getConsoleSender().sendMessage("§5[UltimateBackpacks] §5╚════════════════════════════════════════╝");
    }

    public static UltimateBackpacks getInstance() {
        return instance;
    }

    public static BackpackStorageProvider getStorageProvider() {
        return storageProvider;
    }
}