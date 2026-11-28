
package id.attributesrpg;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SkillExecutionManager {

    private static final String SKILL_DAMAGE_KEY =
            "attributesrpg_skill_damage";

    private final AttributesRPG plugin;

    public SkillExecutionManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public boolean activate(Player player, int slot) {
        if (slot < 1 || slot > SkillManager.MAX_EQUIPPED_SKILLS) {
            player.sendMessage("§cGunakan slot skill 1 sampai 3.");
            return false;
        }

        List<RpgSkill> skills = plugin.getSkillManager()
                .getEquippedSkills(player);

        if (skills.size() < slot) {
            player.sendMessage("§cBelum ada skill di slot tersebut.");
            return false;
        }

        RpgSkill skill = skills.get(slot - 1);
        SkillManager manager = plugin.getSkillManager();

        if (!manager.isUnlocked(player, skill)) {
            player.sendMessage("§cSkill belum terbuka.");
            return false;
        }

        if (manager.isOnCooldown(player, skill)) {
            player.sendMessage("§cSkill cooldown: "
                    + manager.getRemainingCooldown(player, skill)
                    + " detik.");
            return false;
        }

        int level = manager.getSkillLevel(player, skill);

        if (!execute(player, skill, level)) {
            return false;
        }

        manager.startCooldown(player, skill);
        player.sendMessage("§aSkill digunakan: §e"
                + skill.getDisplayName());
        return true;
    }

    private boolean execute(Player player, RpgSkill skill, int level) {
        return switch (skill) {
            case HEAVY_STRIKE -> heavyStrike(player, level);
            case IRON_WALL -> ironWall(player, level);
            case SECOND_WIND -> secondWind(player, level);
            case DASH -> dash(player, level);
            case ARCANE_BURST -> arcaneBurst(player, level);
            case LUCKY_STRIKE -> luckyStrike(player, level);
            case UNSTOPPABLE -> unstoppable(player, level);
            case PRECISION_SHOT -> precisionShot(player, level);
        };
    }

    private void dealSkillDamage(Player attacker,
                                Player target,
                                double damage) {
        target.setMetadata(
                SKILL_DAMAGE_KEY,
                new FixedMetadataValue(plugin, true)
        );

        try {
            target.damage(damage, attacker);
        } finally {
            target.removeMetadata(SKILL_DAMAGE_KEY, plugin);
        }
    }

    private boolean heavyStrike(Player player, int level) {
        Player target = findTarget(player, 4.0, 0.65);

        if (target == null) {
            player.sendMessage("§cTidak ada target di depanmu.");
            return false;
        }

        dealSkillDamage(player, target, 4.0 + (level * 2.0));

        target.setVelocity(target.getVelocity().add(
                player.getLocation().getDirection()
                        .multiply(0.45).setY(0.18)));

        target.getWorld().spawnParticle(
                Particle.CRIT, target.getLocation().add(0, 1, 0),
                18, 0.4, 0.5, 0.4, 0.15);

        player.playSound(player.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1f, 0.8f);
        return true;
    }

    private boolean ironWall(Player player, int level) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                60 + (level * 40), 0, false, true, true));

        player.getWorld().spawnParticle(
                Particle.CLOUD, player.getLocation().add(0, 1, 0),
                25, 0.8, 0.7, 0.8, 0.04);

        player.playSound(player.getLocation(),
                Sound.ITEM_SHIELD_BLOCK, 1f, 0.8f);
        return true;
    }

    private boolean secondWind(Player player, int level) {
        double heal = Math.min(2.0 + (level * 2.0),
                player.getMaxHealth() - player.getHealth());

        if (heal <= 0) {
            player.sendMessage("§cHealth-mu masih penuh.");
            return false;
        }

        player.setHealth(player.getHealth() + heal);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION,
                40 + (level * 20), 0, false, true, true));

        player.getWorld().spawnParticle(
                Particle.HEART, player.getLocation().add(0, 1, 0),
                8, 0.5, 0.5, 0.5, 0);

        player.playSound(player.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.4f);
        return true;
    }

    private boolean dash(Player player, int level) {
        Vector direction = player.getLocation().getDirection()
                .setY(0).normalize();

        player.setVelocity(direction.multiply(
                1.0 + (level * 0.25)).setY(0.25));

        player.getWorld().spawnParticle(
                Particle.CLOUD, player.getLocation(),
                20, 0.3, 0.3, 0.3, 0.08);

        player.playSound(player.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 1.5f);
        return true;
    }

    private boolean arcaneBurst(Player player, int level) {
        List<Player> targets = getNearbyPlayers(player, 5.0);

        if (targets.isEmpty()) {
            player.sendMessage("§cTidak ada pemain di sekitar.");
            return false;
        }

        for (Player target : targets) {
            dealSkillDamage(player, target, 3.0 + (level * 2.0));
        }

        player.getWorld().spawnParticle(
                Particle.ENCHANTED_HIT,
                player.getLocation().add(0, 1, 0),
                45, 2.0, 1.0, 2.0, 0.2);

        player.playSound(player.getLocation(),
                Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 0.8f);
        return true;
    }

    private boolean luckyStrike(Player player, int level) {
        Player target = findTarget(player, 4.0, 0.65);

        if (target == null) {
            player.sendMessage("§cTidak ada target di depanmu.");
            return false;
        }

        double bonus = ThreadLocalRandom.current()
                .nextDouble(2.0, 4.0 + (level * 2.0));

        dealSkillDamage(player, target, bonus);

        target.getWorld().spawnParticle(
                Particle.CRIT, target.getLocation().add(0, 1, 0),
                25, 0.4, 0.5, 0.4, 0.2);

        player.playSound(player.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.2f);
        return true;
    }

    private boolean unstoppable(Player player, int level) {
        int duration = 60 + (level * 40);

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                duration, 0, false, true, true));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                duration, 0, false, true, true));

        player.getWorld().spawnParticle(
                Particle.SOUL, player.getLocation().add(0, 1, 0),
                30, 0.7, 0.8, 0.7, 0.05);

        player.playSound(player.getLocation(),
                Sound.ITEM_TOTEM_USE, 0.8f, 1.2f);
        return true;
    }

    private boolean precisionShot(Player player, int level) {
        Player target = findTarget(player, 12.0, 0.85);

        if (target == null) {
            player.sendMessage("§cTidak ada target dalam bidikan.");
            return false;
        }

        if (!player.hasLineOfSight(target)) {
            player.sendMessage("§cTarget terhalang.");
            return false;
        }

        dealSkillDamage(player, target, 3.0 + (level * 2.0));

        target.getWorld().spawnParticle(
                Particle.CRIT, target.getLocation().add(0, 1, 0),
                15, 0.3, 0.4, 0.3, 0.1);

        player.playSound(player.getLocation(),
                Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1.2f);
        return true;
    }

    private Player findTarget(Player player, double range,
                              double minimumDot) {
        Vector direction = player.getLocation()
                .getDirection().normalize();

        return getNearbyPlayers(player, range).stream()
                .filter(target -> {
                    Vector toTarget = target.getEyeLocation()
                            .toVector()
                            .subtract(player.getEyeLocation().toVector());

                    if (toTarget.lengthSquared() == 0) {
                        return false;
                    }

                    return direction.dot(toTarget.normalize())
                            >= minimumDot;
                })
                .min(Comparator.comparingDouble(
                        target -> target.getLocation()
                                .distanceSquared(player.getLocation())))
                .orElse(null);
    }

    private List<Player> getNearbyPlayers(Player player, double range) {
        return player.getNearbyEntities(range, range, range).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(target -> !target.equals(player))
                .filter(target -> !target.isDead())
                .toList();
    }
}
