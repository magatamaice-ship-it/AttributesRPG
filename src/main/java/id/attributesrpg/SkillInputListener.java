
package id.attributesrpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillInputListener implements Listener {

    private static final long DOUBLE_SHIFT_INTERVAL = 350L;

    private final AttributesRPG plugin;
    private final Map<UUID, Long> lastShiftPress = new HashMap<>();

    public SkillInputListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkillClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.isSneaking()
                || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR
                || action == Action.RIGHT_CLICK_BLOCK) {

            event.setCancelled(true);
            activateSkill(player, 1);
            return;
        }

        if (action == Action.LEFT_CLICK_AIR
                || action == Action.LEFT_CLICK_BLOCK) {

            event.setCancelled(true);
            activateSkill(player, 2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDoubleShift(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        Long previousTime = lastShiftPress.get(uuid);

        if (previousTime != null
                && currentTime - previousTime
                <= DOUBLE_SHIFT_INTERVAL) {

            lastShiftPress.remove(uuid);
            activateSkill(player, 3);

        } else {
            lastShiftPress.put(uuid, currentTime);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastShiftPress.remove(event.getPlayer().getUniqueId());
    }

    private void activateSkill(Player player, int slot) {
        SkillExecutionManager executionManager =
                plugin.getSkillExecutionManager();

        if (executionManager != null) {
            executionManager.activate(player, slot);
        }
    }
}
