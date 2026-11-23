
package id.attributesrpg;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class AttributeEffectListener implements Listener {

    private final AttributesRPG plugin;

    public AttributeEffectListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        Player attacker = getAttackingPlayer(damager);

        if (attacker != null) {
            PlayerData data = getData(attacker);

            if (data != null) {
                double bonus = 0;

                if (damager instanceof Projectile) {
                    bonus = data.getAttribute("dexterity")
                            * plugin.getConfig().getDouble(
                            "attributes.dexterity.ranged-damage-per-level",
                            0.004
                    );
                } else {
                    bonus = data.getAttribute("strength")
                            * plugin.getConfig().getDouble(
                            "attributes.strength.damage-per-level",
                            0.005
                    );
                }

                EntityDamageEvent.DamageCause cause =
                        event.getCause();

                if (cause == EntityDamageEvent.DamageCause.MAGIC
                        || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) {
                    bonus += data.getAttribute("intelligence")
                            * plugin.getConfig().getDouble(
                            "attributes.intelligence.magic-damage-per-level",
                            0.005
                    );
                }

                event.setDamage(event.getDamage() * (1 + bonus));
            }
        }

        if (victim instanceof Player player) {
            PlayerData data = getData(player);

            if (data != null) {
                double reduction = data.getAttribute("defense")
                        * plugin.getConfig().getDouble(
                        "attributes.defense.reduction-per-level",
                        0.003
                );

                reduction = Math.min(reduction, 0.95);

                event.setDamage(
                        event.getDamage() * (1 - reduction)
                );
            }
        }
    }

    private Player getAttackingPlayer(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
            return player;
        }

        return null;
    }

    private PlayerData getData(Player player) {
        return plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());
    }
}
