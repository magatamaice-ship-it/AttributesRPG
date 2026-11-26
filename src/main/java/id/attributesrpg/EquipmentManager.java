
package id.attributesrpg;

import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipmentManager {

    private final AttributesRPG plugin;

    private final Map<UUID, Map<EquipmentType, EquipmentData>> equipped =
            new HashMap<>();

    private static final Map<EquipmentStat, Double> STAT_CAPS =
            new EnumMap<>(EquipmentStat.class);

    static {
        STAT_CAPS.put(EquipmentStat.STRENGTH, 15.0);
        STAT_CAPS.put(EquipmentStat.DEFENSE, 10.0);
        STAT_CAPS.put(EquipmentStat.VITALITY, 10.0);
        STAT_CAPS.put(EquipmentStat.AGILITY, 10.0);
        STAT_CAPS.put(EquipmentStat.INTELLIGENCE, 15.0);
        STAT_CAPS.put(EquipmentStat.LUCK, 10.0);
        STAT_CAPS.put(EquipmentStat.ENDURANCE, 10.0);
        STAT_CAPS.put(EquipmentStat.DEXTERITY, 15.0);

        STAT_CAPS.put(EquipmentStat.CRITICAL_CHANCE, 10.0);
        STAT_CAPS.put(EquipmentStat.CRITICAL_DAMAGE, 25.0);
        STAT_CAPS.put(EquipmentStat.HEALTH, 4.0);
        STAT_CAPS.put(EquipmentStat.MOVEMENT_SPEED, 5.0);
    }

    public EquipmentManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public boolean equip(
            Player player,
            EquipmentData equipment
    ) {
        if (player == null || equipment == null
                || equipment.getType() == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        Map<EquipmentType, EquipmentData> items =
                equipped.computeIfAbsent(
                        uuid,
                        key -> new EnumMap<>(EquipmentType.class)
                );

        items.put(equipment.getType(), equipment);
        return true;
    }

    public boolean unequip(Player player, EquipmentType type) {
        if (player == null || type == null) {
            return false;
        }

        Map<EquipmentType, EquipmentData> items =
                equipped.get(player.getUniqueId());

        if (items == null) {
            return false;
        }

        boolean removed = items.remove(type) != null;

        if (items.isEmpty()) {
            equipped.remove(player.getUniqueId());
        }

        return removed;
    }

    public EquipmentData getEquipped(
            Player player,
            EquipmentType type
    ) {
        if (player == null || type == null) {
            return null;
        }

        Map<EquipmentType, EquipmentData> items =
                equipped.get(player.getUniqueId());

        return items == null ? null : items.get(type);
    }

    public double getTotalStat(
            Player player,
            EquipmentStat stat
    ) {
        if (player == null || stat == null) {
            return 0.0;
        }

        Map<EquipmentType, EquipmentData> items =
                equipped.get(player.getUniqueId());

        if (items == null) {
            return 0.0;
        }

        double total = 0.0;

        for (EquipmentData item : items.values()) {
            total += item.getScaledStat(stat);
        }

        double cap = STAT_CAPS.getOrDefault(stat, 0.0);
        return Math.min(total, cap);
    }

    public Map<EquipmentType, EquipmentData> getEquippedItems(
            Player player
    ) {
        if (player == null) {
            return Map.of();
        }

        Map<EquipmentType, EquipmentData> items =
                equipped.get(player.getUniqueId());

        if (items == null) {
            return Map.of();
        }

        return Map.copyOf(items);
    }

    public void clearPlayer(Player player) {
        if (player != null) {
            equipped.remove(player.getUniqueId());
        }
    }

    public void clearAll() {
        equipped.clear();
    }
}
