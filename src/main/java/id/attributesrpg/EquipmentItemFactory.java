
package id.attributesrpg;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EquipmentItemFactory {

    private final NamespacedKey equipmentKey;
    private final NamespacedKey idKey;
    private final NamespacedKey typeKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey statsKey;

    private final LegacyComponentSerializer serializer =
            LegacyComponentSerializer.legacySection();

    public EquipmentItemFactory(AttributesRPG plugin) {
        equipmentKey = new NamespacedKey(plugin, "rpg_equipment");
        idKey = new NamespacedKey(plugin, "equipment_id");
        typeKey = new NamespacedKey(plugin, "equipment_type");
        rarityKey = new NamespacedKey(plugin, "equipment_rarity");
        statsKey = new NamespacedKey(plugin, "equipment_stats");
    }

    public ItemStack createItem(EquipmentData data) {
        if (!isValidData(data)) {
            throw new IllegalArgumentException(
                    "Data equipment tidak valid!"
            );
        }

        ItemStack item = new ItemStack(getMaterial(data.getType()));
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            throw new IllegalStateException(
                    "Gagal membuat metadata equipment."
            );
        }

        String color = data.getRarity().getColor();

        meta.displayName(serializer.deserialize(
                color + data.getDisplayName()
        ));

        List<Component> lore = new ArrayList<>();

        lore.add(serializer.deserialize(
                "§7Tipe: §f" + data.getType().getDisplayName()
        ));

        lore.add(serializer.deserialize(
                "§7Rarity: " + color
                        + data.getRarity().getDisplayName()
        ));

        lore.add(serializer.deserialize(""));

        for (Map.Entry<EquipmentStat, Double> entry
                : data.getStats().entrySet()) {

            double value = data.getScaledStat(entry.getKey());

            lore.add(serializer.deserialize(
                    "§a+" + String.format(Locale.US, "%.2f", value)
                            + " " + entry.getKey().getDisplayName()
            ));
        }

        lore.add(serializer.deserialize(""));
        lore.add(serializer.deserialize("§8AttributesRPG Equipment"));

        meta.lore(lore);

        meta.getPersistentDataContainer().set(
                equipmentKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        meta.getPersistentDataContainer().set(
                idKey,
                PersistentDataType.STRING,
                data.getId()
        );

        meta.getPersistentDataContainer().set(
                typeKey,
                PersistentDataType.STRING,
                data.getType().name()
        );

        meta.getPersistentDataContainer().set(
                rarityKey,
                PersistentDataType.STRING,
                data.getRarity().name()
        );

        meta.getPersistentDataContainer().set(
                statsKey,
                PersistentDataType.STRING,
                serializeStats(data)
        );

        item.setItemMeta(meta);
        return item;
    }

    public boolean isEquipment(ItemStack item) {
        if (item == null || item.getType().isAir()
                || !item.hasItemMeta()) {
            return false;
        }

        Byte marker = item.getItemMeta()
                .getPersistentDataContainer()
                .get(equipmentKey, PersistentDataType.BYTE);

        return marker != null && marker == (byte) 1;
    }

    private boolean isValidData(EquipmentData data) {
        if (data == null
                || data.getId() == null
                || data.getId().isBlank()
                || data.getDisplayName() == null
                || data.getDisplayName().isBlank()
                || data.getType() == null
                || data.getRarity() == null
                || data.getStats() == null
                || data.getStats().isEmpty()) {
            return false;
        }

        for (Map.Entry<EquipmentStat, Double> entry
                : data.getStats().entrySet()) {

            if (entry.getKey() == null
                    || entry.getValue() == null
                    || !Double.isFinite(entry.getValue())
                    || entry.getValue() <= 0
                    || !Double.isFinite(
                            data.getScaledStat(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }

    private String serializeStats(EquipmentData data) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<EquipmentStat, Double> entry
                : data.getStats().entrySet()) {

            if (result.length() > 0) {
                result.append(";");
            }

            result.append(entry.getKey().name())
                    .append("=")
                    .append(entry.getValue());
        }

        return result.toString();
    }

    private Material getMaterial(EquipmentType type) {
        return switch (type) {
            case WEAPON -> Material.NETHERITE_SWORD;
            case HELMET -> Material.NETHERITE_HELMET;
            case CHESTPLATE -> Material.NETHERITE_CHESTPLATE;
            case LEGGINGS -> Material.NETHERITE_LEGGINGS;
            case BOOTS -> Material.NETHERITE_BOOTS;
            case ACCESSORY -> Material.AMETHYST_SHARD;
        };
    }
}
