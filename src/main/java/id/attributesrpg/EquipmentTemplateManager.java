
package id.attributesrpg;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EquipmentTemplateManager {

    private final AttributesRPG plugin;
    private final File file;

    private final Map<String, EquipmentData> templates =
            new HashMap<>();

    public EquipmentTemplateManager(AttributesRPG plugin) {
        this.plugin = plugin;
        this.file = new File(
                plugin.getDataFolder(),
                "equipment.yml"
        );

        load();
    }

    public void load() {
        templates.clear();

        if (!file.exists()) {
            saveFile();
            return;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section =
                config.getConfigurationSection("templates");

        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            try {
                String path = "templates." + id;

                String name = config.getString(
                        path + ".display-name"
                );

                String typeName = config.getString(
                        path + ".type"
                );

                String rarityName = config.getString(
                        path + ".rarity"
                );

                if (name == null || typeName == null
                        || rarityName == null) {
                    continue;
                }

                EquipmentType type = EquipmentType.valueOf(
                        typeName.toUpperCase(Locale.ROOT)
                );

                EquipmentRarity rarity = EquipmentRarity.valueOf(
                        rarityName.toUpperCase(Locale.ROOT)
                );

                Map<EquipmentStat, Double> stats =
                        new EnumMap<>(EquipmentStat.class);

                ConfigurationSection statSection =
                        config.getConfigurationSection(
                                path + ".stats"
                        );

                if (statSection != null) {
                    for (String statName :
                            statSection.getKeys(false)) {

                        EquipmentStat stat =
                                EquipmentStat.valueOf(
                                        statName.toUpperCase(Locale.ROOT)
                                );

                        double value = statSection.getDouble(
                                statName
                        );

                        if (Double.isFinite(value) && value > 0) {
                            stats.put(stat, value);
                        }
                    }
                }

                EquipmentData data = new EquipmentData(
                        id,
                        name,
                        type,
                        rarity,
                        stats
                );

                if (!isValid(data)) {
                    plugin.getLogger().warning(
                            "Template tidak valid dilewati: " + id
                    );
                    continue;
                }

                templates.put(id.toLowerCase(Locale.ROOT), data);

            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning(
                        "Template equipment tidak valid: " + id
                );
            }
        }

        plugin.getLogger().info(
                "Memuat " + templates.size()
                        + " template equipment."
        );
    }

    public boolean saveTemplate(EquipmentData data) {
        if (!isValid(data)) {
            return false;
        }

        String id = data.getId().toLowerCase(Locale.ROOT);

        if (templates.containsKey(id)) {
            return false;
        }

        templates.put(id, data);

        if (!saveFile()) {
            templates.remove(id);
            return false;
        }

        return true;
    }

    private boolean isValid(EquipmentData data) {
        if (data == null
                || data.getId() == null
                || data.getId().isBlank()
                || data.getDisplayName() == null
                || data.getDisplayName().isBlank()
                || data.getType() == null
                || data.getRarity() == null
                || data.getStats().isEmpty()) {
            return false;
        }

        if (!data.getId().matches("[a-zA-Z0-9_-]+")) {
            return false;
        }

        for (Map.Entry<EquipmentStat, Double> entry :
                data.getStats().entrySet()) {

            EquipmentStat stat = entry.getKey();
            Double value = entry.getValue();

            if (stat == null || value == null
                    || !Double.isFinite(value) || value <= 0) {
                return false;
            }

            double cap = EquipmentManager.getStatCap(stat);

            if (cap <= 0
                    || value * data.getRarity().getBonusMultiplier()
                    > cap) {
                return false;
            }
        }

        return true;
    }

    public EquipmentData getTemplate(String id) {
        if (id == null) {
            return null;
        }

        return templates.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean deleteTemplate(String id) {
        if (id == null) {
            return false;
        }

        String key = id.toLowerCase(Locale.ROOT);
        EquipmentData removed = templates.remove(key);

        if (removed == null) {
            return false;
        }

        if (!saveFile()) {
            templates.put(key, removed);
            return false;
        }

        return true;
    }

    public Collection<EquipmentData> getTemplates() {
        return Collections.unmodifiableCollection(
                templates.values()
        );
    }

    public boolean saveFile() {
        YamlConfiguration config =
                new YamlConfiguration();

        for (Map.Entry<String, EquipmentData> entry :
                templates.entrySet()) {

            EquipmentData data = entry.getValue();
            String path = "templates." + entry.getKey();

            config.set(
                    path + ".display-name",
                    data.getDisplayName()
            );

            config.set(
                    path + ".type",
                    data.getType().name()
            );

            config.set(
                    path + ".rarity",
                    data.getRarity().name()
            );

            for (Map.Entry<EquipmentStat, Double> stat :
                    data.getStats().entrySet()) {

                config.set(
                        path + ".stats." + stat.getKey().name(),
                        stat.getValue()
                );
            }
        }

        try {
            if (!plugin.getDataFolder().exists()
                    && !plugin.getDataFolder().mkdirs()) {
                return false;
            }

            config.save(file);
            return true;

        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan equipment.yml: "
                            + exception.getMessage()
            );
            return false;
        }
    }
}
