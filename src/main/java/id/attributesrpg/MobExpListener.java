
package id.attributesrpg;

import org.bukkit.entity.Entity;
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
        Entity mob = event.getEntity();
        Player killer = mob.getKiller();

        UUID mobId = mob.getUniqueId();
        boolean fromSpawner = spawnerMobs.remove(mobId);

        if (killer == null) {
            return;
        }

        if (fromSpawner && !plugin.getConfig().getBoolean(
                "mob-exp.spawner-exp-enabled", false)) {
            return;
        }

        String mobName = mob.getType().name();

        int exp = plugin.getConfig().getInt(
                "mob-exp." + mobName, 0
        );

        if (exp <= 0) {
            return;
        }

        plugin.getLevelManager().addExp(killer, exp);

        killer.sendMessage(
                "§a+" + exp + " RPG EXP §7(" + mobName + ")"
        );
    }
}
