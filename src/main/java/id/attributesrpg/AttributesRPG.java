
package id.attributesrpg;

import org.bukkit.plugin.java.JavaPlugin;

public final class AttributesRPG extends JavaPlugin {

    private static AttributesRPG instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("AttributesRPG berhasil diaktifkan!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AttributesRPG berhasil dinonaktifkan!");
    }

    public static AttributesRPG getInstance() {
        return instance;
    }
}
