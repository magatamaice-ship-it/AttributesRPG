package id.attributesrpg;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        if (item == null || item.getType().isAir()
                || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data =
                meta.getPersistentDataContainer();

        Byte marker = data.get(
                equipmentKey,
                PersistentDataType.BYTE
        );

        if (marker == null || marker != (byte) 1) {
            return null;
        }

        String id = data.get(idKey, PersistentDataType.STRING);
        String typeName = data.get(typeKey, PersistentDataType.STRING);
        String rarityName = data.get(rarityKey, PersistentDataType.STRING);
        String serializedStats = data.get(
                statsKey,
                PersistentDataType.STRING
        );

        if (id == null || !id.matches("[a-zA-Z0-9_-]{2,32}")
                || typeName == null
                || rarityName == null
                || serializedStats == null
                || serializedStats.isBlank()) {
            return null;
        }

        EquipmentType type = EquipmentType.fromName(typeName);
        EquipmentRarity rarity =
                EquipmentRarity.fromName(rarityName);

        if (type == null || rarity == null) {
            return null;
        }

        Map<EquipmentStat, Double> stats =
                new EnumMap<>(EquipmentStat.class);

        try {
            String[] entries = serializedStats.split(";", -1);

            for (String entry : entries) {
                if (entry.isBlank()) {
                    return null;
                }

                String[] parts = entry.split("=", -1);

                if (parts.length != 2
                        || parts[0].isBlank()
                        || parts[1].isBlank()) {
                    return null;
                }

                EquipmentStat stat =
                        EquipmentStat.fromName(parts[0].trim());

                if (stat == null || stats.containsKey(stat)) {
                    return null;
                }

                double value = Double.parseDouble(parts[1].trim());

                if (!Double.isFinite(value) || value <= 0) {
                    return null;
                }

                stats.put(stat, value);
            }
        } catch (IllegalArgumentException exception) {
            return null;
        }

        if (stats.isEmpty()) {
            return null;
        }

        String displayName = meta.hasDisplayName()
                ? PlainTextComponentSerializer.plainText()
                    .serialize(meta.displayName())
                : "Equipment";

        if (displayName.isBlank()) {
            displayName = "Equipment";
        }

        return new EquipmentData(
                id,
                displayName,
                type,
                rarity,
                stats
        );
    }
}
