
package id.attributesrpg;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class EquipmentData {

    private final String id;
    private final String displayName;
    private final EquipmentType type;
    private final EquipmentRarity rarity;
    private final Map<EquipmentStat, Double> stats;

    public EquipmentData(
            String id,
            String displayName,
            EquipmentType type,
            EquipmentRarity rarity,
            Map<EquipmentStat, Double> stats
    ) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.displayName = displayName;
        this.type = type;
        this.rarity = rarity == null
                ? EquipmentRarity.COMMON : rarity;

        this.stats = new EnumMap<>(EquipmentStat.class);

        if (stats != null) {
            stats.forEach((stat, value) -> {
                if (stat != null && value != null
                        && Double.isFinite(value) && value > 0) {
                    this.stats.put(stat, value);
                }
            });
        }
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EquipmentType getType() {
        return type;
    }

    public EquipmentRarity getRarity() {
        return rarity;
    }

    public Map<EquipmentStat, Double> getStats() {
        return Collections.unmodifiableMap(stats);
    }

    public double getStat(EquipmentStat stat) {
        return stats.getOrDefault(stat, 0.0);
    }

    public double getScaledStat(EquipmentStat stat) {
        return getStat(stat) * rarity.getBonusMultiplier();
    }
}
