
package id.attributesrpg;

import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EquipmentManager {

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

    private final AttributesRPG plugin;

    private final Map<UUID, Map<EquipmentType, EquipmentData>>
            equipped = new HashMap<>();

    public EquipmentManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public boolean equip(Player player, EquipmentData equipment) {
        if (player == null || equipment == null
                || !isValidEquipment(equipment)) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        Map<EquipmentType, EquipmentData> playerEquipment =
                equipped.computeIfAbsent(
                        uuid, key -> new EnumMap<>(EquipmentType.class)
                );

        if (playerEquipment.containsKey(equipment.getType())) {
            return false;
        }

        try {
            plugin.getDatabaseManager()
                    .saveEquipment(uuid, equipment);

            playerEquipment.put(equipment.getType(), equipment);
            return true;

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan equipment "
                            + player.getName() + ": "
                            + exception.getMessage()
            );
            return false;
        }
    }

    public boolean unequip(Player player, EquipmentType type) {
        if (player == null || type == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        Map<EquipmentType, EquipmentData> playerEquipment =
                equipped.get(uuid);

        if (playerEquipment == null
                || !playerEquipment.containsKey(type)) {
            return false;
        }

        try {
            plugin.getDatabaseManager()
                    .removeEquipment(uuid, type);

            playerEquipment.remove(type);

            if (playerEquipment.isEmpty()) {
                equipped.remove(uuid);
            }

            return true;

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal melepas equipment "
                            + player.getName() + ": "
                            + exception.getMessage()
            );
            return false;
        }
    }

    public void loadPlayer(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        try {
            List<EquipmentData> loaded =
                    plugin.getDatabaseManager().loadEquipment(uuid);

            Map<EquipmentType, EquipmentData> playerEquipment =
                    new EnumMap<>(EquipmentType.class);

            for (EquipmentData equipment : loaded) {
                if (!isValidEquipment(equipment)) {
                    plugin.getLogger().warning(
                            "Equipment tidak valid dilewati: "
                                    + equipment.getId()
                    );
                    continue;
                }

                if (playerEquipment.containsKey(equipment.getType())) {
                    plugin.getLogger().warning(
                            "Equipment duplikat dilewati: "
                                    + equipment.getType()
                    );
                    continue;
                }

                playerEquipment.put(
                        equipment.getType(), equipment
                );
            }

            if (playerEquipment.isEmpty()) {
                equipped.remove(uuid);
            } else {
                equipped.put(uuid, playerEquipment);
            }

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal memuat equipment "
                            + player.getName() + ": "
                            + exception.getMessage()
            );
        }
    }

    public EquipmentData getEquipped(
            Player player,
            EquipmentType type
    ) {
        if (player == null || type == null) {
            return null;
        }

        Map<EquipmentType, EquipmentData> playerEquipment =
                equipped.get(player.getUniqueId());

        if (playerEquipment == null) {
            return null;
        }

        return playerEquipment.get(type);
    }

    public List<EquipmentData> getEquippedItems(Player player) {
        if (player == null) {
            return List.of();
        }

        Map<EquipmentType, EquipmentData> playerEquipment =
                equipped.get(player.getUniqueId());

        if (playerEquipment == null) {
            return List.of();
        }

        return new ArrayList<>(playerEquipment.values());
    }

    public double getTotalStat(
            Player player,
            EquipmentStat stat
    ) {
        if (player == null || stat == null) {
            return 0.0;
        }

        Map<EquipmentType, EquipmentData> playerEquipment =
                equipped.get(player.getUniqueId());

        if (playerEquipment == null) {
            return 0.0;
        }

        double total = 0.0;

        for (EquipmentData equipment : playerEquipment.values()) {
            double value = equipment.getScaledStat(stat);

            if (!Double.isFinite(value) || value <= 0) {
                continue;
            }

            total += value;
        }

        double cap = getStatCap(stat);

        return Math.min(total, cap);
    }

    public void clearPlayer(Player player) {
        if (player == null) {
            return;
        }

        equipped.remove(player.getUniqueId());
    }

    public void clearAll() {
        equipped.clear();
    }

    public boolean isValidEquipment(EquipmentData equipment) {
        if (equipment == null
                || equipment.getId() == null
                || equipment.getId().isBlank()
                || equipment.getDisplayName() == null
                || equipment.getDisplayName().isBlank()
                || equipment.getType() == null
                || equipment.getRarity() == null
                || equipment.getStats() == null
                || equipment.getStats().isEmpty()) {
            return false;
        }

        for (Map.Entry<EquipmentStat, Double> entry
                : equipment.getStats().entrySet()) {

            EquipmentStat stat = entry.getKey();
            Double value = entry.getValue();

            if (stat == null || value == null
                    || !Double.isFinite(value) || value <= 0) {
                return false;
            }

            double scaled = equipment.getScaledStat(stat);
            double cap = getStatCap(stat);

            if (!Double.isFinite(scaled)
                    || scaled <= 0 || scaled > cap) {
                return false;
            }
        }

        return true;
    }

    public static double getStatCap(EquipmentStat stat) {
        if (stat == null) {
            return 0.0;
        }

        return STAT_CAPS.getOrDefault(stat, 0.0);
    }
}
