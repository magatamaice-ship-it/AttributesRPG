
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

    private static final String SKILL_DAMAGE_KEY =
            "attributesrpg_skill_damage";

    private final AttributesRPG plugin;

    public AttributeEffectListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = getPlayerAttacker(event);

        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        double damage = event.getDamage();

        if (!Double.isFinite(damage) || damage <= 0) {
            return;
        }

        PlayerData victimData = plugin.getPlayerDataManager()
                .getPlayer(victim.getUniqueId());

        if (victimData == null) {
            return;
        }

        boolean skillDamage = victim.hasMetadata(SKILL_DAMAGE_KEY);

        PlayerData attackerData = plugin.getPlayerDataManager()
                .getPlayer(attacker.getUniqueId());

        if (attackerData == null) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();

        boolean ranged = event.getDamager() instanceof Projectile;

        boolean melee =
                cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;

        // Bonus atribut dan critical hanya untuk serangan normal.
        if (!skillDamage) {

            if (cause == EntityDamageEvent.DamageCause.MAGIC
                    || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) {

                double attributeBonus = getBonus(
                        attackerData,
                        "intelligence",
                        "attributes.intelligence.magic-damage-per-level",
                        0.005,
                        0.25
                );

                double equipmentBonus = getEquipmentBonus(
                        attacker,
                        EquipmentStat.INTELLIGENCE
                );

                damage *= 1.0 + attributeBonus + equipmentBonus;

            } else if (ranged) {

                double attributeBonus = getBonus(
                        attackerData,
                        "dexterity",
                        "attributes.dexterity.ranged-damage-per-level",
                        0.004,
                        0.20
                );

                double equipmentBonus = getEquipmentBonus(
                        attacker,
                        EquipmentStat.DEXTERITY
                );

                damage *= 1.0 + attributeBonus + equipmentBonus;

            } else if (melee) {

                double attributeBonus = getBonus(
                        attackerData,
                        "strength",
                        "attributes.strength.damage-per-level",
                        0.005,
                        0.25
                );

                double equipmentBonus = getEquipmentBonus(
                        attacker,
                        EquipmentStat.STRENGTH
                );

                damage *= 1.0 + attributeBonus + equipmentBonus;
            }

            if (melee || ranged) {
                damage = plugin.getCriticalHitManager().applyCritical(
                        attacker,
                        victim,
                        attackerData,
                        ranged,
                        damage
                );
            }
        }

        // Defense tetap berlaku untuk serangan normal dan skill.
        double attributeReduction = getBonus(
                victimData,
                "defense",
                "attributes.defense.reduction-per-level",
                0.003,
                0.15
        );

        double equipmentReduction = getEquipmentBonus(
                victim,
                EquipmentStat.DEFENSE
        );

        double totalReduction = Math.min(
                0.75,
                attributeReduction + equipmentReduction
        );

        damage *= 1.0 - totalReduction;

        if (!Double.isFinite(damage)) {
            return;
        }

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

    private double getEquipmentBonus(
            Player player,
            EquipmentStat stat
    ) {
        double value = plugin.getEquipmentManager()
                .getTotalStat(player, stat);

        if (!Double.isFinite(value)) {
            return 0;
        }

        return Math.max(0, value) / 100.0;
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

        if (!Double.isFinite(perLevel) || perLevel < 0) {
            perLevel = defaultValue;
        }

        double bonus = data.getAttribute(attribute) * perLevel;

        if (!Double.isFinite(bonus)) {
            return 0;
        }

        return Math.max(0, Math.min(bonus, maximum));
    }
}
