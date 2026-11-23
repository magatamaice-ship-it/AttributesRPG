
package id.attributesrpg;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LevelUpEffect {

    private final AttributesRPG plugin;

    public LevelUpEffect(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public void play(Player player) {
        if (!plugin.getConfig().getBoolean(
                "level-up.particles.enabled", true)) {
            return;
        }

        int duration = plugin.getConfig().getInt(
                "level-up.particles.duration-seconds", 3
        );

        int count = plugin.getConfig().getInt(
                "level-up.particles.count", 30
        );

        double radius = plugin.getConfig().getDouble(
                "level-up.particles.radius", 1.2
        );

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline() || tick >= duration * 20) {
                    cancel();
                    return;
                }

                double angle = tick * 0.35;

                for (int i = 0; i < count; i++) {
                    double rotation = angle
                            + (Math.PI * 2 * i / count);

                    double x = Math.cos(rotation) * radius;
                    double z = Math.sin(rotation) * radius;
                    double y = (i % 10) * 0.2;

                    player.getWorld().spawnParticle(
                            Particle.HAPPY_VILLAGER,
                            player.getLocation().add(x, y, z),
                            1,
                            0, 0, 0,
                            0
                    );
                }

                tick += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
