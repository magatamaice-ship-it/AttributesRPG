
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class KnockListener implements Listener {

    private final AttributesRPG plugin;

    public KnockListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (plugin.getKnockManager().isKnocked(player)) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getKnockManager().hasReviveProtection(player)) {
            event.setCancelled(true);
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();

        boolean fallDamage =
                cause == EntityDamageEvent.DamageCause.FALL;

        boolean playerCombat = event instanceof
                org.bukkit.event.entity.EntityDamageByEntityEvent
                damageEvent
                && (damageEvent.getDamager() instanceof Player
                || damageEvent.getDamager() instanceof
                org.bukkit.entity.Projectile projectile
                && projectile.getShooter() instanceof Player);

        if (!fallDamage && !playerCombat) {
            return;
        }

        if (player.getHealth() <= event.getFinalDamage()) {
            event.setCancelled(true);

            Bukkit.getScheduler().runTask(
                    plugin,
                    () -> plugin.getKnockManager().startKnock(player)
            );
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getKnockManager().isKnocked(event.getPlayer())) {
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        if (!plugin.getKnockManager().isKnocked(target)) {
            return;
        }

        event.setCancelled(true);

        Player helper = event.getPlayer();

        if (plugin.getKnockManager().startRevive(helper, target)) {
            helper.sendMessage("§eProses membantu dimulai. Jangan bergerak selama 8 detik.");
        } else {
            helper.sendMessage("§cTidak bisa membantu pemain tersebut sekarang.");
        }
    }
  
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (!event.isSneaking()
                || !plugin.getKnockManager().isKnocked(player)) {
            return;
        }

        event.setCancelled(true);

        KnockMenu.open(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getKnockManager().handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        plugin.getKnockManager().cancelRevive(
                event.getEntity().getUniqueId()
        );
    }
}
