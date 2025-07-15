package dev.yours4nty.ultimatebackpacks.utils;

// Java imports
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class InventorySerializer {

    // Serializes an Inventory to a byte array
    public static byte[] serialize(Inventory inventory) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeInt(inventory.getSize());
            oos.writeObject(inventory.getContents());
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Deserializes a byte array back into an Inventory
    public static Inventory deserialize(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
            int size = ois.readInt();
            ItemStack[] items = (ItemStack[]) ois.readObject();
            Inventory inventory = Bukkit.createInventory(null, size);
            inventory.setContents(items);
            return inventory;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return Bukkit.createInventory(null, 54); // Fallback
        }
    }
}
