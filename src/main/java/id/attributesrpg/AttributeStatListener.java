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
        this.vitalityKey = new NamespacedKey(plugin, "rpg_vitality");
        this.agilityKey = new NamespacedKey(plugin, "rpg_agility");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyStats(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> applyStats(event.getPlayer())
        );
    }

    public void applyStats(Player player) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            return;
        }

        EquipmentManager equipment = plugin.getEquipmentManager();

        double equipmentVitality = equipment.getTotalStat(
                player,
                EquipmentStat.VITALITY
        );

        double equipmentHealth = equipment.getTotalStat(
                player,
                EquipmentStat.HEALTH
        );

        double healthPerLevel = plugin.getConfig().getDouble(
                "attributes.vitality.health-per-level", 0.2
        );

        double healthBonus =
                (data.getAttribute("vitality") + equipmentVitality)
                        * healthPerLevel
                        + equipmentHealth;

        double equipmentAgility = equipment.getTotalStat(
                player,
                EquipmentStat.AGILITY
        );

        double equipmentSpeed = equipment.getTotalStat(
                player,
                EquipmentStat.MOVEMENT_SPEED
        );

        double speedPerLevel = plugin.getConfig().getDouble(
                "attributes.agility.speed-per-level", 0.002
        );

        double speedBonus =
                (data.getAttribute("agility") + equipmentAgility)
                        * speedPerLevel
                        + equipmentSpeed / 100.0;

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

        if (healthAttribute != null
                && player.getHealth() > healthAttribute.getValue()) {
            player.setHealth(healthAttribute.getValue());
        }
    }

    private void applyModifier(
            Player player,
            Attribute attribute,
            NamespacedKey key,
            double amount,
            AttributeModifier.Operation operation
    ) {
        AttributeInstance instance = player.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        instance.removeModifier(key);

        if (amount <= 0) {
            return;
        }

        instance.addModifier(new AttributeModifier(
                key,
                amount,
                operation
        ));
    }
}
