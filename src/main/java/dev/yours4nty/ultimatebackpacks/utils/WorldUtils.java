package dev.yours4nty.ultimatebackpacks.utils;

// Bukkit Imports
import org.bukkit.World;

// Custom Import statements
import dev.yours4nty.ultimatebackpacks.UltimateBackpacks;

import java.util.List;

public class WorldUtils {

    private static List<String> blacklistedWorlds;

    /**
     * Loads the blacklisted worlds from the plugin's configuration.
     */
    public static void loadWorldBlacklist() {
        blacklistedWorlds = UltimateBackpacks.getInstance().getConfig().getStringList("WorldSettings.BlacklistWorlds");
    }

    /**
     * Checks if a given world is blacklisted.
     *
     * @param world The world to check.
     * @return true if the world is blacklisted, false otherwise.
     */
    public static boolean isBlacklisted(World world) {
        return blacklistedWorlds.contains(world.getName());
    }
}
