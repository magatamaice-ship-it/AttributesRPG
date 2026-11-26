
package id.attributesrpg;

public enum EquipmentStat {

    STRENGTH("Strength", "Kekuatan"),
    DEFENSE("Defense", "Pertahanan"),
    VITALITY("Vitality", "Vitalitas"),
    AGILITY("Agility", "Kelincahan"),
    INTELLIGENCE("Intelligence", "Kecerdasan"),
    LUCK("Luck", "Keberuntungan"),
    ENDURANCE("Endurance", "Daya Tahan"),
    DEXTERITY("Dexterity", "Ketangkasan"),

    CRITICAL_CHANCE("Critical Chance", "Peluang Critical"),
    CRITICAL_DAMAGE("Critical Damage", "Damage Critical"),
    HEALTH("Health", "Nyawa"),
    MOVEMENT_SPEED("Movement Speed", "Kecepatan Gerak");

    private final String displayName;
    private final String description;

    EquipmentStat(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static EquipmentStat fromName(String name) {
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
