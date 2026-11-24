
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelManager {

    private final AttributesRPG plugin;

    private final Map<UUID, BossBar> expBars = new HashMap<>();
    private final Map<UUID, BukkitTask> expTasks = new HashMap<>();

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
            removeExpBar(player.getUniqueId());
            return;
        }

        long totalExp = (long) data.getExp() + amount;
        data.setExp((int) Math.min(totalExp, Integer.MAX_VALUE));

        int pointsPerLevel = plugin.getConfig().getInt(
                "settings.points-per-level", 1
        );

        while (data.getLevel() < maxLevel) {
            int requiredExp = getRequiredExp(data.getLevel());

            if (requiredExp <= 0 || data.getExp() < requiredExp) {
                break;
            }

            data.setExp(data.getExp() - requiredExp);
            data.setLevel(data.getLevel() + 1);

            data.setAttributePoints(
                    data.getAttributePoints() + pointsPerLevel
            );

            showLevelUp(player, data.getLevel());
        }

        if (data.getLevel() >= maxLevel) {
            data.setExp(0);
            removeExpBar(player.getUniqueId());
        } else {
            updateExpBar(player, data);
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

    private void updateExpBar(Player player, PlayerData data) {
        UUID uuid = player.getUniqueId();

        int requiredExp = getRequiredExp(data.getLevel());

        if (requiredExp <= 0) {
            return;
        }

        int currentExp = data.getExp();
        int remainingExp = Math.max(0, requiredExp - currentExp);

        double progress = Math.max(
                0.0,
                Math.min(1.0, (double) currentExp / requiredExp)
        );

        BossBar bar = expBars.get(uuid);

        if (bar == null) {
            bar = Bukkit.createBossBar(
                    "",
                    BarColor.GREEN,
                    BarStyle.SEGMENTED_10
            );

            expBars.put(uuid, bar);
        }

        String title = ChatColor.translateAlternateColorCodes(
                '&',
                "&a&lLEVEL " + data.getLevel()
                        + " &8| &f" + currentExp
                        + "&7/&f" + requiredExp
                        + " EXP &8| &e" + remainingExp
                        + " EXP lagi"
        );

        bar.setTitle(title);
        bar.setProgress(progress);
        bar.setVisible(true);

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        BukkitTask oldTask = expTasks.remove(uuid);

        if (oldTask != null) {
            oldTask.cancel();
        }

        BukkitTask newTask = Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    expTasks.remove(uuid);

                    if (!player.isOnline()) {
                        removeExpBar(uuid);
                        return;
                    }

                    removeExpBar(uuid);
                },
                100L
        );

        expTasks.put(uuid, newTask);
    }

    private void removeExpBar(UUID uuid) {
        BukkitTask task = expTasks.remove(uuid);

        if (task != null) {
            task.cancel();
        }

        BossBar bar = expBars.remove(uuid);

        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
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
