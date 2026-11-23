
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LevelManager {

    private final AttributesRPG plugin;
    private final LevelUpEffect levelUpEffect;

    public LevelManager(AttributesRPG plugin) {
        this.plugin = plugin;
        this.levelUpEffect = new LevelUpEffect(plugin);
    }

    public int getRequiredExp(int level) {
        int base = plugin.getConfig().getInt(
                "leveling.base-exp", 100
        );

        int increase = plugin.getConfig().getInt(
                "leveling.exp-per-level", 20
        );

        return base + (increase * level);
    }

    public void addExp(Player player, int amount) {
        if (amount <= 0) return;

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) return;

        int maxLevel = plugin.getConfig().getInt(
                "settings.max-player-level", 100
        );

        if (data.getLevel() >= maxLevel) {
            data.setExp(0);
            return;
        }

        data.setExp(data.getExp() + amount);

        while (data.getLevel() < maxLevel) {
            int required = getRequiredExp(data.getLevel());

            if (data.getExp() < required) break;

            data.setExp(data.getExp() - required);
            data.setLevel(data.getLevel() + 1);

            int points = plugin.getConfig().getInt(
                    "settings.points-per-level", 1
            );

            data.setAttributePoints(
                    data.getAttributePoints() + points
            );

            playLevelUp(player, data.getLevel());
        }

        if (data.getLevel() >= maxLevel) {
            data.setExp(0);
        }
    }

    private void playLevelUp(Player player, int level) {
        String title = plugin.getConfig().getString(
                "level-up.title", "&6&lLEVEL UP!"
        );

        String subtitle = plugin.getConfig().getString(
                "level-up.subtitle", "&eLevel baru: &f{level}"
        );

        subtitle = subtitle.replace(
                "{level}", String.valueOf(level)
        );

        player.sendTitle(
                ChatColor.translateAlternateColorCodes('&', title),
                ChatColor.translateAlternateColorCodes('&', subtitle),
                10, 50, 15
        );

        try {
            Sound sound = Sound.valueOf(
                    plugin.getConfig().getString(
                            "level-up.sound",
                            "ENTITY_PLAYER_LEVELUP"
                    )
            );

            player.playSound(
                    player.getLocation(),
                    sound,
                    (float) plugin.getConfig().getDouble(
                            "level-up.sound-volume", 1.0
                    ),
                    (float) plugin.getConfig().getDouble(
                            "level-up.sound-pitch", 1.0
                    )
            );

        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning(
                    "Sound level-up tidak valid!"
            );
        }

        levelUpEffect.play(player);
    }
}
