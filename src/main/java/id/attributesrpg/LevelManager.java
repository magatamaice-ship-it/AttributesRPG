
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LevelManager {

    private final AttributesRPG plugin;

    public LevelManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public int getRequiredExp(int level) {
        int baseExp = plugin.getConfig().getInt(
                "leveling.base-exp", 100
        );

        int expPerLevel = plugin.getConfig().getInt(
                "leveling.exp-per-level", 20
        );

        return baseExp + expPerLevel * level;
    }

    public void addExp(Player player, int amount) {
        if (amount <= 0) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            return;
        }

        int maxLevel = plugin.getConfig().getInt(
                "settings.max-player-level", 100
        );

        if (data.getLevel() >= maxLevel) {
            return;
        }

        long totalExp = (long) data.getExp() + amount;
        data.setExp((int) Math.min(totalExp, Integer.MAX_VALUE));

        int pointsPerLevel = plugin.getConfig().getInt(
                "settings.points-per-level", 1
        );

        boolean leveledUp = false;

        while (data.getLevel() < maxLevel) {
            int requiredExp = getRequiredExp(data.getLevel());

            if (data.getExp() < requiredExp) {
                break;
            }

            data.setExp(data.getExp() - requiredExp);
            data.setLevel(data.getLevel() + 1);

            data.setAttributePoints(
                    data.getAttributePoints() + pointsPerLevel
            );

            leveledUp = true;

            showLevelUp(player, data.getLevel());
        }

        if (data.getLevel() >= maxLevel) {
            data.setExp(0);
        }

        try {
            plugin.getPlayerDataManager().savePlayer(data);
        } catch (Exception exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan EXP pemain "
                            + player.getName()
            );
            exception.printStackTrace();
        }
    }

    private void showLevelUp(Player player, int level) {
        String title = ChatColor.translateAlternateColorCodes(
                '&',
                plugin.getConfig().getString(
                        "level-up.title", "&6&lLEVEL UP!"
                )
        );

        String subtitle = ChatColor.translateAlternateColorCodes(
                '&',
                plugin.getConfig().getString(
                        "level-up.subtitle", "&eLevel baru: &f{level}"
                ).replace("{level}", String.valueOf(level))
        );

        player.sendTitle(title, subtitle, 10, 50, 15);

        String soundName = plugin.getConfig().getString(
                "level-up.sound", "ENTITY_PLAYER_LEVELUP"
        );

        try {
            player.playSound(
                    player.getLocation(),
                    Sound.valueOf(soundName),
                    (float) plugin.getConfig().getDouble(
                            "level-up.sound-volume", 1.0
                    ),
                    (float) plugin.getConfig().getDouble(
                            "level-up.sound-pitch", 1.0
                    )
            );
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning(
                    "Sound level-up tidak valid: " + soundName
            );
        }

        new LevelUpEffect(plugin).play(player);
    }
}
