
package id.attributesrpg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String playerName;

    private int level;
    private int exp;
    private int attributePoints;

    private final Map<String, Integer> attributes = new LinkedHashMap<>();

    private static final String[] ATTRIBUTE_NAMES = {
        "strength",
        "defense",
        "vitality",
        "agility",
        "intelligence",
        "luck",
        "endurance",
        "dexterity"
    };

    public PlayerData(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;

        for (String attribute : ATTRIBUTE_NAMES) {
            attributes.put(attribute, 0);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = Math.max(0, exp);
    }

    public int getAttributePoints() {
        return attributePoints;
    }

    public void setAttributePoints(int points) {
        this.attributePoints = Math.max(0, points);
    }

    public int getAttribute(String name) {
        return attributes.getOrDefault(
                name.toLowerCase(), 0
        );
    }

    public void setAttribute(String name, int value) {
        String key = name.toLowerCase();

        if (attributes.containsKey(key)) {
            attributes.put(key, Math.max(0, value));
        }
    }

    public Map<String, Integer> getAttributes() {
        return new LinkedHashMap<>(attributes);
    }

    public int getTotalAllocatedPoints() {
        return attributes.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}
