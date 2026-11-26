
package id.attributesrpg;

public enum EquipmentType {

    WEAPON("Senjata"),
    HELMET("Helm"),
    CHESTPLATE("Chestplate"),
    LEGGINGS("Leggings"),
    BOOTS("Boots"),
    ACCESSORY("Aksesori");

    private final String displayName;

    EquipmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EquipmentType fromName(String name) {
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
