package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

public class ItemBuilder {

    public static ItemStack createBackpackItem() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        // Set the skull owner to a custom profile with a skin URL
        try {
            URL url = new URL("http://textures.minecraft.net/texture/b9b18b02453db1ba2564c1f9c967fdaa4f3f8f95d697265d4bde6620d68deeab");
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            profile.getTextures().setSkin(url);
            meta.setOwnerProfile(profile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Set the display name for the backpack item
        String name = MessageHandler.get("backpack-item-name");
        meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
        skull.setItemMeta(meta);
        return skull;
    }
}
