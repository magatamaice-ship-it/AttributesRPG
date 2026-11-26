
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
        if (data == null || data.getType() == null) {
            throw new IllegalArgumentException(
                    "EquipmentData atau EquipmentType tidak valid!"
            );
        }

        ItemStack item = new ItemStack(getMaterial(data.getType()));
        ItemMeta meta = item.getItemMeta();

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

            double value = entry.getValue()
                    * data.getRarity().getBonusMultiplier();

            lore.add(serializer.deserialize(
                    "§a+" + String.format("%.2f", value)
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
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        return item.getItemMeta()
                .getPersistentDataContainer()
                .has(equipmentKey, PersistentDataType.BYTE);
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
