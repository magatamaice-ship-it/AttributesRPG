
package id.attributesrpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LuckEnduranceListener implements Listener {

    private final AttributesRPG plugin;

    private static final Set<String> NEGATIVE_EFFECTS = Set.of(
            "poison",
            "wither",
            "slowness",
            "mining_fatigue",
            "weakness",
            "blindness",
            "hunger",
            "nausea",
            "darkness",
            "levitation",
            "bad_omen",
            "raid_omen",
            "trial_omen"
    );

    public LuckEnduranceListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null || event.getDrops().isEmpty()) {
            return;
        }

        PlayerData data = getData(killer);

        if (data == null) {
            return;
        }

        double chance = data.getAttribute("luck")
                * plugin.getConfig().getDouble(
                "attributes.luck.loot-chance-per-level",
                0.002
        );

        chance = Math.min(chance, 0.10);

        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }

        List<ItemStack> drops = event.getDrops();

        ItemStack selected = drops.get(
                ThreadLocalRandom.current().nextInt(drops.size())
        );

        event.getEntity().getWorld().dropItemNaturally(
                event.getEntity().getLocation(),
                selected.clone()
        );
    }

    @EventHandler
    public void onNegativeEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PotionEffect effect = event.getNewEffect();

        if (effect == null) {
            return;
        }

        String effectName = effect.getType()
                .getKey()
                .getKey();

        if (!NEGATIVE_EFFECTS.contains(effectName)) {
            return;
        }

        PlayerData data = getData(player);

        if (data == null) {
            return;
        }

        double chance = data.getAttribute("endurance")
                * plugin.getConfig().getDouble(
                "attributes.endurance.effect-resistance-per-level",
                0.003
        );

        chance = Math.min(chance, 0.15);

        if (ThreadLocalRandom.current().nextDouble() < chance) {
            event.setCancelled(true);
        }
    }

    private PlayerData getData(Player player) {
        return plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());
    }
}
