
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
        if (!Double.isFinite(damage) || damage <= 0) {
            return damage;
        }

        if (!plugin.getConfig().getBoolean(
                "combat.critical.enabled", true)) {
            return damage;
        }

        EquipmentManager equipment = plugin.getEquipmentManager();

        double baseChance = getConfigValue(
                "combat.critical.base-chance", 0.05
        );

        double luckChance = getConfigValue(
                "combat.critical.luck-chance-per-level", 0.005
        );

        double equipmentChance = equipment.getTotalStat(
                attacker,
                EquipmentStat.CRITICAL_CHANCE
        ) / 100.0;

        double chance = baseChance
                + data.getAttribute("luck") * luckChance
                + equipmentChance;

        double maxChance = Math.max(0, Math.min(
                getConfigValue("combat.critical.max-chance", 0.30),
                1.0
        ));

        chance = Math.max(0, Math.min(chance, maxChance));

        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return damage;
        }

        double criticalBonus = getConfigValue(
                "combat.critical.base-damage-bonus", 0.50
        );

        if (ranged) {
            criticalBonus += data.getAttribute("dexterity")
                    * getConfigValue(
                    "combat.critical.dexterity-damage-per-level",
                    0.01
            );
        } else {
            criticalBonus += data.getAttribute("strength")
                    * getConfigValue(
                    "combat.critical.strength-damage-per-level",
                    0.01
            );
        }

        criticalBonus += equipment.getTotalStat(
                attacker,
                EquipmentStat.CRITICAL_DAMAGE
        ) / 100.0;

        double maxBonus = Math.max(0, getConfigValue(
                "combat.critical.max-damage-bonus", 1.00
        ));

        criticalBonus = Math.max(
                0,
                Math.min(criticalBonus, maxBonus)
        );

        showCriticalEffect(attacker, victim);

        return damage * (1.0 + criticalBonus);
    }

    private double getConfigValue(String path, double defaultValue) {
        double value = plugin.getConfig().getDouble(path, defaultValue);

        return Double.isFinite(value) && value >= 0
                ? value
                : defaultValue;
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
