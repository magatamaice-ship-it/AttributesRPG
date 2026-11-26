
package id.attributesrpg;

public enum EquipmentRarity {

    COMMON("Common", 1.0, "§f"),
    UNCOMMON("Uncommon", 1.15, "§a"),
    RARE("Rare", 1.35, "§9"),
    EPIC("Epic", 1.65, "§5"),
    LEGENDARY("Legendary", 2.0, "§6"),
    MYTHIC("Mythic", 2.5, "§c");

    private final String displayName;
    private final double bonusMultiplier;
    private final String color;

    EquipmentRarity(
            String displayName,
            double bonusMultiplier,
            String color
    ) {
        this.displayName = displayName;
        this.bonusMultiplier = bonusMultiplier;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBonusMultiplier() {
        return bonusMultiplier;
    }

    public String getColor() {
        return color;
    }

    public static EquipmentRarity fromName(String name) {
        if (name == null) {
            return COMMON;
        }

        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return COMMON;
        }
    }
}
