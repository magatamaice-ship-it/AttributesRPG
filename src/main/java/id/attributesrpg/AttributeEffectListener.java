
package id.attributesrpg;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

public class AttributeEffectListener implements Listener {

    private final AttributesRPG plugin;

    public AttributeEffectListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        // Sistem Combat AttributesRPG hanya berlaku untuk PvP.
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = getPlayerAttacker(event);

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        double damage = event.getDamage();

        if (damage <= 0) {
            return;
        }

        PlayerData attackerData = plugin.getPlayerDataManager()
                .getPlayer(attacker.getUniqueId());

        PlayerData victimData = plugin.getPlayerDataManager()
                .getPlayer(victim.getUniqueId());

        if (attackerData == null || victimData == null) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Bonus damage penyerang.
        if (cause == EntityDamageEvent.DamageCause.MAGIC
                || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) {

            damage *= 1.0 + getBonus(
                    attackerData,
                    "intelligence",
                    "attributes.intelligence.magic-damage-per-level",
                    0.005,
                    0.25
            );

        } else if (event.getDamager() instanceof Projectile) {

            damage *= 1.0 + getBonus(
                    attackerData,
                    "dexterity",
                    "attributes.dexterity.ranged-damage-per-level",
                    0.004,
                    0.20
            );

        } else if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {

            damage *= 1.0 + getBonus(
                    attackerData,
                    "strength",
                    "attributes.strength.damage-per-level",
                    0.005,
                    0.25
            );
        }

        // Pengurangan damage berdasarkan Defense korban.
        double reduction = getBonus(
                victimData,
                "defense",
                "attributes.defense.reduction-per-level",
                0.003,
                0.15
        );

        damage *= 1.0 - reduction;

        event.setDamage(Math.max(0, damage));
    }

    private Player getPlayerAttacker(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player player) {
            return player;
        }

        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    private double getBonus(
            PlayerData data,
            String attribute,
            String configPath,
            double defaultValue,
            double maximum
    ) {
        double perLevel = plugin.getConfig().getDouble(
                configPath,
                defaultValue
        );

        double bonus = data.getAttribute(attribute) * perLevel;

        return Math.max(0, Math.min(bonus, maximum));
    }
}
