
package id.attributesrpg;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class SkillHudTask {

    private final AttributesRPG plugin;
    private BukkitTask task;

    public SkillHudTask(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::updateAll,
                20L,
                20L
        );
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        SkillManager manager = plugin.getSkillManager();
        List<RpgSkill> skills = manager.getEquippedSkills(player);

        StringBuilder hud = new StringBuilder("§6Skills §8| ");

        for (int slot = 1; slot <= 3; slot++) {
            if (slot > skills.size()) {
                hud.append("§7[").append(slot).append(": -] ");
                continue;
            }

            RpgSkill skill = skills.get(slot - 1);

            hud.append("§e[").append(slot).append(": ")
                    .append(skill.getDisplayName());

            if (manager.isOnCooldown(player, skill)) {
                hud.append(" §c")
                        .append(manager.getRemainingCooldown(player, skill))
                        .append("s");
            } else {
                hud.append(" §aREADY");
            }

            hud.append("§e] ");
        }

        player.sendActionBar(Component.text(
                hud.toString().replaceAll("§[0-9a-fk-or]", "")
        ));
    }
 
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
