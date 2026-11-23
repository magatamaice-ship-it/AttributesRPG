
package id.attributesrpg;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class AttributesRPG extends JavaPlugin {

    private static AttributesRPG instance;

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();

            playerDataManager = new PlayerDataManager(databaseManager);

            getServer().getPluginManager().registerEvents(
        new PlayerDataListener(this), this
);

        } catch (SQLException exception) {
            getLogger().severe(
                    "Gagal mengaktifkan database AttributesRPG!"
            );
            exception.printStackTrace();

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("AttributesRPG berhasil diaktifkan!");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("AttributesRPG berhasil dinonaktifkan!");
    }

    public static AttributesRPG getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
