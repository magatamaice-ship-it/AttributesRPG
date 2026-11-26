package id.attributesrpg;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class CriticalHitManager {

    private final AttributesRPG plugin;

    public CriticalHitManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public double applyCritical(
            Player attacker,
            Player victim,
            PlayerData data,
            boolean ranged,
            double damage
    ) {
        if (!plugin.getConfig().getBoolean(
                "combat.critical.enabled", true)) {
            return damage;
        }

        EquipmentManager equipment = plugin.getEquipmentManager();

        // Peluang critical dasar dan bonus Luck.
        double chance = plugin.getConfig().getDouble(
                "combat.critical.base-chance", 0.05
        );

        chance += data.getAttribute("luck")
                * plugin.getConfig().getDouble(
                "combat.critical.luck-chance-per-level", 0.005
        );

        // Bonus Critical Chance dari Equipment.
        chance += equipment.getTotalStat(
                attacker.getUniqueId(),
                EquipmentStat.CRITICAL_CHANCE
        ) / 100.0;

        double maxChance = plugin.getConfig().getDouble(
                "combat.critical.max-chance", 0.30
        );

        chance = Math.max(0, Math.min(chance, maxChance));

        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return damage;
        }

        // Bonus critical damage dasar.
        double criticalBonus = plugin.getConfig().getDouble(
                "combat.critical.base-damage-bonus", 0.50
        );

        if (ranged) {
            criticalBonus += data.getAttribute("dexterity")
                    * plugin.getConfig().getDouble(
                    "combat.critical.dexterity-damage-per-level",
                    0.01
            );
        } else {
            criticalBonus += data.getAttribute("strength")
                    * plugin.getConfig().getDouble(
                    "combat.critical.strength-damage-per-level",
                    0.01
            );
        }

        // Bonus Critical Damage dari Equipment.
        criticalBonus += equipment.getTotalStat(
                attacker.getUniqueId(),
                EquipmentStat.CRITICAL_DAMAGE
        ) / 100.0;

        double maxBonus = plugin.getConfig().getDouble(
                "combat.critical.max-damage-bonus", 1.00
        );

        criticalBonus = Math.max(
                0,
                Math.min(criticalBonus, maxBonus)
        );

        showCriticalEffect(attacker, victim);

        return damage * (1.0 + criticalBonus);
    }

    private void showCriticalEffect(
            Player attacker,
            Player victim
    ) {
        victim.getWorld().spawnParticle(
                Particle.CRIT,
                victim.getLocation().add(0, 1, 0),
                25,
                0.45,
                0.65,
                0.45,
                0.15
        );

        victim.getWorld().spawnParticle(
                Particle.ENCHANTED_HIT,
                victim.getLocation().add(0, 1, 0),
                12,
                0.35,
                0.45,
                0.35,
                0.1
        );

        victim.getWorld().playSound(
                victim.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_CRIT,
                1.0f,
                0.8f
        );

        attacker.sendMessage("§6§lCRITICAL HIT!");
    }
}
