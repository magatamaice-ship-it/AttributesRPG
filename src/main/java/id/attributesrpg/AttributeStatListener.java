
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

        vitalityKey = new NamespacedKey(plugin, "rpg_vitality");
        agilityKey = new NamespacedKey(plugin, "rpg_agility");
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

        applyModifier(
                player,
                Attribute.MAX_HEALTH,
                vitalityKey,
                data.getAttribute("vitality")
                        * plugin.getConfig().getDouble(
                        "attributes.vitality.health-per-level",
                        0.2
                ),
                AttributeModifier.Operation.ADD_NUMBER
        );

        applyModifier(
                player,
                Attribute.MOVEMENT_SPEED,
                agilityKey,
                data.getAttribute("agility")
                        * plugin.getConfig().getDouble(
                        "attributes.agility.speed-per-level",
                        0.002
                ),
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );

        AttributeInstance health = player.getAttribute(
                Attribute.MAX_HEALTH
        );

        if (health != null
                && player.getHealth() > health.getValue()) {
            player.setHealth(health.getValue());
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

        AttributeModifier oldModifier = instance.getModifier(key);

        if (oldModifier != null) {
            instance.removeModifier(oldModifier);
        }

        if (amount > 0) {
            instance.addModifier(
                    new AttributeModifier(key, amount, operation)
            );
        }
    }
}
