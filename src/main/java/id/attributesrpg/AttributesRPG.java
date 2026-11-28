
package id.attributesrpg;

import org.bukkit.command.PluginCommand;
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

    private SkillManager skillManager;
    private SkillExecutionManager skillExecutionManager;
    private SkillMenu skillMenu;
    private SkillHudTask skillHudTask;

    private EquipmentManager equipmentManager;
    private EquipmentItemFactory equipmentItemFactory;
    private EquipmentItemParser equipmentItemParser;
    private EquipmentMenu equipmentMenu;
    private EquipmentTemplateManager equipmentTemplateManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            initializeManagers();
            registerListeners();
            registerCommands();

            skillHudTask.start();

        } catch (SQLException exception) {
            getLogger().severe(
                    "Gagal mengaktifkan database!"
            );
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;

        } catch (RuntimeException exception) {
            getLogger().severe(
                    "Gagal menginisialisasi AttributesRPG!"
            );
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info(
                "AttributesRPG berhasil diaktifkan!"
        );
    }

    private void initializeManagers() throws SQLException {
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        playerDataManager = new PlayerDataManager(databaseManager);
        levelManager = new LevelManager(this);
        attributeManager = new AttributeManager(this);
        attributeStatListener = new AttributeStatListener(this);
        resetItemManager = new ResetItemManager(this);
        criticalHitManager = new CriticalHitManager(this);
        knockManager = new KnockManager(this);

        skillManager = new SkillManager(this);
        skillExecutionManager = new SkillExecutionManager(this);
        skillMenu = new SkillMenu(this);
        skillHudTask = new SkillHudTask(this);

        equipmentManager = new EquipmentManager(this);
        equipmentItemFactory = new EquipmentItemFactory(this);
        equipmentItemParser = new EquipmentItemParser(this);

        equipmentMenu = new EquipmentMenu(
                equipmentManager,
                equipmentItemFactory,
                equipmentItemParser
        );

        equipmentTemplateManager =
                new EquipmentTemplateManager(this);
    }

    private void registerListeners() {
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

        getServer().getPluginManager().registerEvents(
                new SkillCombatListener(this), this
        );

        getServer().getPluginManager().registerEvents(
                skillMenu, this
        );

        getServer().getPluginManager().registerEvents(
                equipmentMenu, this
        );
    }

    private void registerCommands() {
        requireCommand("attributes").setExecutor(
                new AttributesCommand(this)
        );

        requireCommand("stats").setExecutor(
                new StatsCommand(this)
        );

        requireCommand("level").setExecutor(
                new LevelCommand(this)
        );

        requireCommand("setitem").setExecutor(
                new SetItemCommand(this)
        );

        requireCommand("mobexp").setExecutor(
                new MobExpCommand(this)
        );

        PlayerAdminCommand adminCommand =
                new PlayerAdminCommand(this);

        requireCommand("setlevel").setExecutor(adminCommand);
        requireCommand("addlevel").setExecutor(adminCommand);
        requireCommand("addexp").setExecutor(adminCommand);
        requireCommand("setexp").setExecutor(adminCommand);
        requireCommand("givepoints").setExecutor(adminCommand);

        requireCommand("setattribute").setExecutor(
                new SetAttributeCommand(this)
        );

        requireCommand("resetattribute").setExecutor(
                new ResetAttributeCommand(this)
        );

        SkillCommand skillCommand = new SkillCommand(
                skillManager,
                skillExecutionManager,
                skillMenu
        );

        requireCommand("skill").setExecutor(skillCommand);
        requireCommand("skill").setTabCompleter(skillCommand);

        EquipmentCommand equipmentCommand =
                new EquipmentCommand(equipmentMenu);

        requireCommand("equipment").setExecutor(equipmentCommand);
        requireCommand("equipment").setTabCompleter(equipmentCommand);

        RpgItemCommand rpgItemCommand =
                new RpgItemCommand(this);

        requireCommand("rpgitem").setExecutor(rpgItemCommand);
        requireCommand("rpgitem").setTabCompleter(rpgItemCommand);
    }

    private PluginCommand requireCommand(String name) {
        PluginCommand command = getCommand(name);

        if (command == null) {
            throw new IllegalStateException(
                    "Command '" + name
                            + "' tidak ditemukan di plugin.yml!"
            );
        }

        return command;
    }

    @Override
    public void onDisable() {
        if (skillHudTask != null) {
            skillHudTask.stop();
        }

        if (knockManager != null) {
            knockManager.shutdown();
        }

        if (equipmentManager != null) {
            equipmentManager.clearAll();
        }

        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        if (instance == this) {
            instance = null;
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

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SkillExecutionManager getSkillExecutionManager() {
        return skillExecutionManager;
    }

    public EquipmentManager getEquipmentManager() {
        return equipmentManager;
    }

    public EquipmentItemFactory getEquipmentItemFactory() {
        return equipmentItemFactory;
    }

    public EquipmentItemParser getEquipmentItemParser() {
        return equipmentItemParser;
    }

    public EquipmentMenu getEquipmentMenu() {
        return equipmentMenu;
    }

    public EquipmentTemplateManager getEquipmentTemplateManager() {
        return equipmentTemplateManager;
    }
}
