package dev.yours4nty.ultimatebackpacks.storage;

// Java imports
import java.sql.*;
import java.util.UUID;

// Bukkit imports
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

// Custom imports
import dev.yours4nty.ultimatebackpacks.UltimateBackpacks;
import dev.yours4nty.ultimatebackpacks.utils.Config;
import dev.yours4nty.ultimatebackpacks.utils.InventorySerializer;

public class MySQLBackpackStorageProvider implements BackpackStorageProvider {

    private Connection connection;

    @Override
    public Inventory loadBackpack(UUID playerUUID, int index) {
        connect();

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT contents FROM backpacks WHERE uuid = ? AND idx = ?")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, index);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] data = rs.getBytes("contents");
                return InventorySerializer.deserialize(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Bukkit.createInventory(null, 54, "Backpack #" + index);
    }

    @Override
    public void saveBackpack(UUID playerUUID, int index, Inventory inventory) {
        connect();

        try (PreparedStatement stmt = connection.prepareStatement(
                "REPLACE INTO backpacks (uuid, idx, contents) VALUES (?, ?, ?)")) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, index);
            stmt.setBytes(3, InventorySerializer.serialize(inventory));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getMaxBackpacks(Player player) {
        for (int i = 10; i >= 0; i--) {
            if (player.hasPermission("ultimatebackpacks.limit." + i)) return i;
        }
        return 0;
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establishes a connection to the MySQL database and creates the backpacks table if it doesn't exist.
     */
    private void connect() {
        if (connection != null) return;

        try {
            String host = Config.get("storage.mysql.host");
            int port = Integer.parseInt(Config.get("storage.mysql.port"));
            String database = Config.get("storage.mysql.database");
            String username = Config.get("storage.mysql.username");
            String password = Config.get("storage.mysql.password");


            String url = "jdbc:mysql://" + host + "/" + database + "?useSSL=false";

            connection = DriverManager.getConnection(url, username, password);

            Statement stmt = connection.createStatement();
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS backpacks (
                    uuid VARCHAR(36) NOT NULL,
                    idx INT NOT NULL,
                    contents TEXT,
                    PRIMARY KEY (uuid, idx)
                );
            """);

        } catch (SQLException e) {
            Bukkit.getLogger().severe("[UltimateBackpacks] Could not connect to MySQL database.");
            e.printStackTrace();
        }
    }
}
