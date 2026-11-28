
package id.attributesrpg;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class AttributeStatListener implements Listener {

    private final AttributesRPG plugin;
    private final NamespacedKey vitalityKey;
    private final NamespacedKey agilityKey;

    public AttributeStatListener(AttributesRPG plugin) {
        this.plugin = plugin;

        this.vitalityKey = new NamespacedKey(
                plugin, "rpg_vitality"
        );

        this.agilityKey = new NamespacedKey(
                plugin, "rpg_agility"
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scheduleApply(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        scheduleApply(event.getPlayer());
    }

    private void scheduleApply(Player player) {
        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {
                    if (player.isOnline()) {
                        applyStats(player);
                    }
                }
        );
    }

    public void applyStats(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        EquipmentManager equipment = plugin.getEquipmentManager();

        if (data == null || equipment == null) {
            return;
        }

        double playerVitality =
                data.getAttribute("vitality");

        double equipmentVitality = equipment.getTotalStat(
                player,
                EquipmentStat.VITALITY
        );

        double equipmentHealth = equipment.getTotalStat(
                player,
                EquipmentStat.HEALTH
        );

        double healthPerLevel = plugin.getConfig().getDouble(
                "attributes.vitality.health-per-level",
                0.2
        );

        double healthBonus =
                (playerVitality + equipmentVitality)
                        * healthPerLevel
                        + equipmentHealth;

        double playerAgility =
                data.getAttribute("agility");

        double equipmentAgility = equipment.getTotalStat(
                player,
                EquipmentStat.AGILITY
        );

        double equipmentSpeed = equipment.getTotalStat(
                player,
                EquipmentStat.MOVEMENT_SPEED
        );

        double speedPerLevel = plugin.getConfig().getDouble(
                "attributes.agility.speed-per-level",
                0.002
        );

        double speedBonus =
                (playerAgility + equipmentAgility)
                        * speedPerLevel
                        + equipmentSpeed / 100.0;

        if (!Double.isFinite(healthBonus)) {
            healthBonus = 0;
        }

        if (!Double.isFinite(speedBonus)) {
            speedBonus = 0;
        }

        healthBonus = Math.max(0, healthBonus);
        speedBonus = Math.max(0, speedBonus);

        applyModifier(
                player,
                Attribute.MAX_HEALTH,
                vitalityKey,
                healthBonus,
                AttributeModifier.Operation.ADD_NUMBER
        );

        applyModifier(
                player,
                Attribute.MOVEMENT_SPEED,
                agilityKey,
                speedBonus,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );

        AttributeInstance healthAttribute =
                player.getAttribute(Attribute.MAX_HEALTH);

        if (healthAttribute != null) {
            double maximumHealth = healthAttribute.getValue();

            if (player.getHealth() > maximumHealth) {
                player.setHealth(maximumHealth);
            }
        }
    }

    private void applyModifier(
            Player player,
            Attribute attribute,
            NamespacedKey key,
            double amount,
            AttributeModifier.Operation operation
    ) {
        AttributeInstance instance =
                player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        instance.removeModifier(key);

        if (!Double.isFinite(amount) || amount <= 0) {
            return;
        }

        instance.addModifier(
                new AttributeModifier(
                        key,
                        amount,
                        operation
                )
        );
    }
}
