
package id.attributesrpg;

import org.bukkit.entity.Player;

import java.sql.SQLException;
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

    public boolean equip(Player player, EquipmentData equipment) {
        if (player == null || !isValidEquipment(equipment)) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        Map<EquipmentType, EquipmentData> items =
                equipped.computeIfAbsent(
                        uuid,
                        key -> new EnumMap<>(EquipmentType.class)
                );

        if (items.containsKey(equipment.getType())) {
            return false;
        }

        try {
            plugin.getDatabaseManager().saveEquipment(uuid, equipment);
            items.put(equipment.getType(), equipment);
            return true;
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan equipment: "
                            + exception.getMessage()
            );
            return false;
        }
    }

    private boolean isValidEquipment(EquipmentData equipment) {
        if (equipment == null
                || equipment.getType() == null
                || equipment.getRarity() == null
                || equipment.getId() == null
                || equipment.getId().isBlank()
                || equipment.getDisplayName() == null
                || equipment.getDisplayName().isBlank()
                || equipment.getStats().isEmpty()) {
            return false;
        }

        for (Map.Entry<EquipmentStat, Double> entry :
                equipment.getStats().entrySet()) {

            EquipmentStat stat = entry.getKey();
            Double value = entry.getValue();

            if (stat == null || value == null
                    || !Double.isFinite(value) || value <= 0) {
                return false;
            }

            double cap = STAT_CAPS.getOrDefault(stat, 0.0);

            if (cap <= 0 || value > cap) {
                return false;
            }

            if (value * equipment.getRarity().getBonusMultiplier()
                    > cap) {
                return false;
            }
        }

        return true;
    }

    public boolean unequip(Player player, EquipmentType type) {
        if (player == null || type == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        Map<EquipmentType, EquipmentData> items = equipped.get(uuid);

        if (items == null || !items.containsKey(type)) {
            return false;
        }

        try {
            plugin.getDatabaseManager().removeEquipment(uuid, type);
            items.remove(type);

            if (items.isEmpty()) {
                equipped.remove(uuid);
            }

            return true;
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menghapus equipment: "
                            + exception.getMessage()
            );
            return false;
        }
    }

    public void loadPlayer(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();

        try {
            Map<EquipmentType, EquipmentData> items =
                    new EnumMap<>(EquipmentType.class);

            for (EquipmentData data :
                    plugin.getDatabaseManager().loadEquipment(uuid)) {

                if (!isValidEquipment(data)) {
                    plugin.getLogger().warning(
                            "Equipment tidak valid dilewati untuk "
                                    + player.getName()
                    );
                    continue;
                }

                items.put(data.getType(), data);
            }

            if (items.isEmpty()) {
                equipped.remove(uuid);
            } else {
                equipped.put(uuid, items);
            }

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal memuat equipment " + player.getName()
                            + ": " + exception.getMessage()
            );
        }
    }

    public EquipmentData getEquipped(
            Player player, EquipmentType type
    ) {
        if (player == null || type == null) return null;

        Map<EquipmentType, EquipmentData> items =
                equipped.get(player.getUniqueId());

        return items == null ? null : items.get(type);
    }

    public double getTotalStat(Player player, EquipmentStat stat) {
        if (player == null || stat == null) return 0.0;

        Map<EquipmentType, EquipmentData> items =
                equipped.get(player.getUniqueId());

        if (items == null) return 0.0;

        double total = 0.0;

        for (EquipmentData item : items.values()) {
            total += item.getScaledStat(stat);
        }

        return Math.min(total, STAT_CAPS.getOrDefault(stat, 0.0));
    }

    public Map<EquipmentType, EquipmentData> getEquippedItems(
            Player player
    ) {
        if (player == null) return Map.of();

        Map<EquipmentType, EquipmentData> items =
                equipped.get(player.getUniqueId());

        return items == null ? Map.of() : Map.copyOf(items);
    }

    public void clearPlayer(Player player) {
        if (player != null) {
            equipped.remove(player.getUniqueId());
        }
    }

    public void clearAll() {
        equipped.clear();
    }

    public static double getStatCap(EquipmentStat stat) {
        return STAT_CAPS.getOrDefault(stat, 0.0);
    }
}
