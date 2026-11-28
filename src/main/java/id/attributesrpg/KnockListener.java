
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

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

        if (plugin.getKnockManager().isKnocked(player)
                || plugin.getKnockManager().hasReviveProtection(player)) {
            event.setCancelled(true);
            return;
        }

        boolean fallDamage =
                event.getCause() == EntityDamageEvent.DamageCause.FALL;

        boolean playerCombat =
                event instanceof EntityDamageByEntityEvent damage
                && (damage.getDamager() instanceof Player
                || damage.getDamager() instanceof Projectile projectile
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKnockedAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker
                && plugin.getKnockManager().isKnocked(attacker)) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter
                && plugin.getKnockManager().isKnocked(shooter)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getKnockManager().isKnocked(event.getPlayer())
                || event.getTo() == null) {
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
        if (!(event.getRightClicked() instanceof Player target)
                || !plugin.getKnockManager().isKnocked(target)) {
            return;
        }

        event.setCancelled(true);

        Player helper = event.getPlayer();

        if (plugin.getKnockManager().isKnocked(helper)) {
            return;
        }

        if (!plugin.getKnockManager().startRevive(helper, target)) {
            helper.sendMessage(
                    "§cTidak bisa membantu pemain tersebut sekarang."
            );
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getKnockManager().isKnocked(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getKnockManager().isKnocked(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (plugin.getKnockManager().isKnocked(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHeldItem(PlayerItemHeldEvent event) {
        if (plugin.getKnockManager().isKnocked(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player player
                && plugin.getKnockManager().isKnocked(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)
                || !plugin.getKnockManager().isKnocked(player)) {
            return;
        }

        if (!event.getView().getTitle().equals("§8Status Knock")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player
                && plugin.getKnockManager().isKnocked(player)
                && !event.getView().getTitle().equals("§8Status Knock")) {
            event.setCancelled(true);
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

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onHelperDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player helper)) {
            return;
        }

        plugin.getKnockManager().cancelReviveForHelper(
                helper.getUniqueId()
        );
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
