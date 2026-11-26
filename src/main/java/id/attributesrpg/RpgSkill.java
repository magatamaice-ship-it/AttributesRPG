
package id.attributesrpg;

public enum RpgSkill {

    HEAVY_STRIKE(
            "Heavy Strike",
            "strength",
            "Serangan kuat untuk meningkatkan damage melee.",
            10,
            25,
            40,
            12
    ),

    IRON_WALL(
            "Iron Wall",
            "defense",
            "Mengurangi damage yang diterima sementara.",
            10,
            25,
            40,
            30
    ),

    SECOND_WIND(
            "Second Wind",
            "vitality",
            "Memulihkan sebagian HP pemain.",
            10,
            25,
            40,
            45
    ),

    DASH(
            "Dash",
            "agility",
            "Melakukan gerakan cepat ke arah depan.",
            10,
            25,
            40,
            15
    ),

    ARCANE_BURST(
            "Arcane Burst",
            "intelligence",
            "Menghasilkan serangan energi kepada pemain lawan.",
            10,
            25,
            40,
            20
    ),

    LUCKY_STRIKE(
            "Lucky Strike",
            "luck",
            "Meningkatkan peluang serangan kritis sementara.",
            10,
            25,
            40,
            35
    ),

    UNSTOPPABLE(
            "Unstoppable",
            "endurance",
            "Memberikan ketahanan terhadap efek negatif.",
            10,
            25,
            40,
            40
    ),

    PRECISION_SHOT(
            "Precision Shot",
            "dexterity",
            "Meningkatkan kekuatan serangan jarak jauh.",
            10,
            25,
            40,
            18
    );
  
    private final String displayName;
    private final String attribute;
    private final String description;
    private final int unlockLevel;
    private final int firstUpgradeLevel;
    private final int secondUpgradeLevel;
    private final int cooldownSeconds;

    RpgSkill(
            String displayName,
            String attribute,
            String description,
            int unlockLevel,
            int firstUpgradeLevel,
            int secondUpgradeLevel,
            int cooldownSeconds
    ) {
        this.displayName = displayName;
        this.attribute = attribute;
        this.description = description;
        this.unlockLevel = unlockLevel;
        this.firstUpgradeLevel = firstUpgradeLevel;
        this.secondUpgradeLevel = secondUpgradeLevel;
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getDescription() {
        return description;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public int getFirstUpgradeLevel() {
        return firstUpgradeLevel;
    }

    public int getSecondUpgradeLevel() {
        return secondUpgradeLevel;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public static RpgSkill fromName(String name) {
        if (name == null) {
            return null;
        }

        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
