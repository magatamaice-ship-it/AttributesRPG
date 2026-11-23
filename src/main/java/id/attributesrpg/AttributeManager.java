
package id.attributesrpg;

import java.util.Set;

public class AttributeManager {

    private final AttributesRPG plugin;

    private static final Set<String> ATTRIBUTES = Set.of(
            "strength",
            "defense",
            "vitality",
            "agility",
            "intelligence",
            "luck",
            "endurance",
            "dexterity"
    );

    public AttributeManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public boolean isValidAttribute(String attribute) {
        return attribute != null
                && ATTRIBUTES.contains(attribute.toLowerCase());
    }

    public Set<String> getAttributes() {
        return ATTRIBUTES;
    }

    public int getMaxLevel(String attribute) {
        if (!isValidAttribute(attribute)) {
            return 0;
        }

        String key = attribute.toLowerCase();

        return plugin.getConfig().getInt(
                "attributes." + key + ".max-level",
                plugin.getConfig().getInt(
                        "settings.max-attribute-level", 50
                )
        );
    }

    public boolean allocatePoint(
            PlayerData data,
            String attribute
    ) {
        if (data == null || !isValidAttribute(attribute)) {
            return false;
        }

        String key = attribute.toLowerCase();

        int currentLevel = data.getAttribute(key);
        int maxLevel = getMaxLevel(key);

        if (data.getAttributePoints() <= 0
                || currentLevel >= maxLevel) {
            return false;
        }

        data.setAttribute(key, currentLevel + 1);
        data.setAttributePoints(data.getAttributePoints() - 1);

        return true;
    }

    public boolean allocatePoints(
            PlayerData data,
            String attribute,
            int amount
    ) {
        if (data == null || amount <= 0
                || !isValidAttribute(attribute)) {
            return false;
        }

        String key = attribute.toLowerCase();

        int currentLevel = data.getAttribute(key);
        int maxLevel = getMaxLevel(key);

        int availablePoints = Math.min(
                data.getAttributePoints(),
                maxLevel - currentLevel
        );

        if (amount > availablePoints) {
            return false;
        }

        data.setAttribute(key, currentLevel + amount);
        data.setAttributePoints(
                data.getAttributePoints() - amount
        );

        return true;
    }

    public int resetAttributes(PlayerData data) {
        if (data == null) {
            return 0;
        }

        int refundedPoints = data.getTotalAllocatedPoints();

        for (String attribute : ATTRIBUTES) {
            data.setAttribute(attribute, 0);
        }

        data.setAttributePoints(
                data.getAttributePoints() + refundedPoints
        );

        return refundedPoints;
    }
}
