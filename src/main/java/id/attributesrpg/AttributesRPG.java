
package id.attributesrpg;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class AttributesRPG extends JavaPlugin {

    private static AttributesRPG instance;

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;
    private AttributeManager attributeManager;
    private AttributeStatListener attributeStatListener;
    private ResetItemManager resetItemManager;
    private CriticalHitManager criticalHitManager;
    private KnockManager knockManager;

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
            attributeStatListener = new AttributeStatListener(this);
            resetItemManager = new ResetItemManager(this);
            criticalHitManager = new CriticalHitManager(this);
            knockManager = new KnockManager(this);

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
                    attributeStatListener, this
            );
            getServer().getPluginManager().registerEvents(
                    new LuckEnduranceListener(this), this
            );
            getServer().getPluginManager().registerEvents(
                    new AttributeMenuListener(), this
            );
            getServer().getPluginManager().registerEvents(
                    new ResetItemListener(this), this
            );
            getServer().getPluginManager().registerEvents(
                    new KnockListener(this), this
            );
            getServer().getPluginManager().registerEvents(
                    new KnockMenu(this), this
            );
            getCommand("attributes").setExecutor(
                    new AttributesCommand(this)
            );
            getCommand("stats").setExecutor(
                    new StatsCommand(this)
            );
            getCommand("level").setExecutor(
                    new LevelCommand(this)
            );
            getCommand("setitem").setExecutor(
                    new SetItemCommand(this)
            );
            getCommand("mobexp").setExecutor(
                    new MobExpCommand(this)
            );

            PlayerAdminCommand adminCommand =
                    new PlayerAdminCommand(this);

            getCommand("setlevel").setExecutor(adminCommand);
            getCommand("addlevel").setExecutor(adminCommand);
            getCommand("addexp").setExecutor(adminCommand);
            getCommand("setexp").setExecutor(adminCommand);
            getCommand("givepoints").setExecutor(adminCommand);

            getCommand("setattribute").setExecutor(
                    new SetAttributeCommand(this)
            );

            getCommand("resetattribute").setExecutor(
                    new ResetAttributeCommand(this)
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
        if (knockManager != null) {
            knockManager.shutdown();
        }

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

    public AttributeStatListener getAttributeStatListener() {
        return attributeStatListener;
    }

    public ResetItemManager getResetItemManager() {
        return resetItemManager;
    }

    public CriticalHitManager getCriticalHitManager() {
        return criticalHitManager;
    }

    public KnockManager getKnockManager() {
        return knockManager;
    }
}
