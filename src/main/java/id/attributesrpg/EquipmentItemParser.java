
package id.attributesrpg;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.Map;

public class EquipmentItemParser {

    private final NamespacedKey equipmentKey;
    private final NamespacedKey idKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey statsKey;

    public EquipmentItemParser(AttributesRPG plugin) {
        equipmentKey = new NamespacedKey(plugin, "rpg_equipment");
        idKey = new NamespacedKey(plugin, "equipment_id");
        typeKey = new NamespacedKey(plugin, "equipment_type");
        rarityKey = new NamespacedKey(plugin, "equipment_rarity");
        statsKey = new NamespacedKey(plugin, "equipment_stats");
    }

    public EquipmentData parse(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data =
                meta.getPersistentDataContainer();

        if (!data.has(equipmentKey, PersistentDataType.BYTE)) {
            return null;
        }

        String id = data.get(idKey, PersistentDataType.STRING);
        String typeName = data.get(typeKey, PersistentDataType.STRING);
        String rarityName = data.get(rarityKey, PersistentDataType.STRING);
        String serializedStats = data.get(
                statsKey, PersistentDataType.STRING
        );

        EquipmentType type = EquipmentType.fromName(typeName);

        if (type == null || id == null || rarityName == null
                || serializedStats == null) {
            return null;
        }

        Map<EquipmentStat, Double> stats =
                new EnumMap<>(EquipmentStat.class);

        try {
            if (!serializedStats.isBlank()) {
                for (String entry : serializedStats.split(";")) {
                    String[] parts = entry.split("=", 2);

                    if (parts.length != 2) {
                        return null;
                    }

                    EquipmentStat stat =
                            EquipmentStat.fromName(parts[0]);

                    double value = Double.parseDouble(parts[1]);

                    if (stat == null || !Double.isFinite(value)
                            || value <= 0) {
                        return null;
                    }

                    stats.put(stat, value);
                }
            }
        } catch (IllegalArgumentException exception) {
            return null;
        }

        return new EquipmentData(
                id,
                meta.hasDisplayName()
                        ? meta.getDisplayName().toString()
                        : "Equipment",
                type,
                EquipmentRarity.fromName(rarityName),
                stats
        );
    }
}
