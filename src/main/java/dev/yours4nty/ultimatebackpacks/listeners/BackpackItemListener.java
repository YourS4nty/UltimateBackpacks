package dev.yours4nty.ultimatebackpacks.listeners;

// Bukkit imports
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

// Custom imports
import dev.yours4nty.ultimatebackpacks.BackpackGUI;
import dev.yours4nty.ultimatebackpacks.utils.MessageHandler;

import net.md_5.bungee.api.ChatColor;

public class BackpackItemListener implements Listener {

    /**
     * Listener for player interactions with the backpack item.
     * Opens the backpack GUI when the player right-clicks with the backpack item.
     *
     * @param event The PlayerInteractEvent triggered by the player.
     */
    @EventHandler
    public void onPlayerUseBackpack(PlayerInteractEvent event) {
        // Solo mano principal
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        String expectedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                MessageHandler.get("backpack-item-name")));

        if (meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals(expectedName)) {
            event.setCancelled(true);
            BackpackGUI.openSelector(player);
        }
    }
}
