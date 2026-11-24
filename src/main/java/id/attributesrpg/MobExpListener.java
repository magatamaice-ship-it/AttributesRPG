
package id.attributesrpg;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobExpListener implements Listener {

    private final AttributesRPG plugin;

    private final Set<UUID> spawnerMobs =
            ConcurrentHashMap.newKeySet();

    public MobExpListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason()
                == CreatureSpawnEvent.SpawnReason.SPAWNER) {

            spawnerMobs.add(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity mob = event.getEntity();

        UUID mobId = mob.getUniqueId();

        boolean spawnedBySpawner = spawnerMobs.remove(mobId);

        if (spawnedBySpawner
                && !plugin.getConfig().getBoolean(
                "mob-exp.spawner-exp-enabled", false)) {
            return;
        }

        Player killer = mob.getKiller();

        if (killer == null) {
            return;
        }

        int exp = plugin.getConfig().getInt(
                "mob-exp." + mob.getType().name(),
                0
        );

        if (exp <= 0) {
            return;
        }

        plugin.getLevelManager().addExp(killer, exp);
    }
}
