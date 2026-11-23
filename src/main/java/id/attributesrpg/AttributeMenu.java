
package id.attributesrpg;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class AttributesRPG extends JavaPlugin {

    private static AttributesRPG instance;

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;
    private AttributeManager attributeManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();

            playerDataManager = new PlayerDataManager(databaseManager);
            levelManager = new LevelManager(this);
            attributeManager = new AttributeManager(this);

            getServer().getPluginManager().registerEvents(
                    new PlayerDataListener(this), this
            );

            getServer().getPluginManager().registerEvents(
                    new MobExpListener(this), this
            );

            getServer().getPluginManager().registerEvents(
                    new AttributeEffectListener(this), this
            );

            getServer().getPluginManager().registerEvents(
                    new AttributeStatListener(this), this
            );

            getServer().getPluginManager().registerEvents(
                    new LuckEnduranceListener(this), this
            );

            getServer().getPluginManager().registerEvents(
                    new AttributeMenuListener(), this
            );

        } catch (SQLException exception) {
            getLogger().severe("Gagal mengaktifkan database!");
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

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public AttributeManager getAttributeManager() {
        return attributeManager;
    }
}
