package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KnockManager {

    private static final long KNOCK_DURATION =
            15 * 60 * 1000L;

    private static final long REVIVE_DURATION = 8 * 20L;
    private static final long REVIVE_PROTECTION = 10 * 1000L;

    private final AttributesRPG plugin;

    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, TextDisplay> displays = new HashMap<>();
    private final Map<UUID, BukkitTask> timers = new HashMap<>();
    private final Map<UUID, ReviveSession> revives = new HashMap<>();
    private final Map<UUID, Long> reviveProtection = new HashMap<>();

    public KnockManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public void startKnock(Player player) {
        PlayerData data = getData(player);

        if (data == null || data.isKnocked()) {
            return;
        }

        data.setKnocked(true);
        data.setKnockExpiresAt(
                System.currentTimeMillis() + KNOCK_DURATION
        );

        player.setHealth(1.0);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setVelocity(player.getVelocity().zero());

        save(data);
        activateKnock(player, data);
    }

    public void restoreKnock(Player player) {
        PlayerData data = getData(player);

        if (data == null || !data.isKnocked()) {
            return;
        }

        if (data.getKnockExpiresAt() <= System.currentTimeMillis()) {
            data.clearKnockState();
            save(data);

            Bukkit.getScheduler().runTask(
                    plugin,
                    () -> player.setHealth(0.0)
            );
            return;
        }

        player.setHealth(Math.min(1.0, player.getHealth()));
        activateKnock(player, data);
    }

    private void activateKnock(Player player, PlayerData data) {
        UUID uuid = player.getUniqueId();

        removeEffects(uuid);

        BossBar bar = Bukkit.createBossBar(
                "§cKNOCKED",
                BarColor.RED,
                BarStyle.SOLID
        );

        bar.addPlayer(player);
        bar.setVisible(true);
        bossBars.put(uuid, bar);

        TextDisplay display = player.getWorld().spawn(
                player.getLocation().add(0, 2.3, 0),
                TextDisplay.class,
                entity -> {
                    entity.setText(
                            "§c§lKNOCKED\n§f" + player.getName()
                    );
                    entity.setBillboard(Display.Billboard.CENTER);
                    entity.setSeeThrough(true);
                    entity.setShadowed(true);
                    entity.setPersistent(false);
                }
        );

        displays.put(uuid, display);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> updateKnock(player),
                0L,
                20L
        );

        timers.put(uuid, task);
    }
  
    private void updateKnock(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = getData(player);

        if (!player.isOnline() || data == null || !data.isKnocked()) {
            removeEffects(uuid);
            return;
        }

        long remaining = data.getKnockExpiresAt()
                - System.currentTimeMillis();

        if (remaining <= 0) {
            cancelRevive(uuid);
            data.clearKnockState();
            save(data);
            removeEffects(uuid);
            player.setHealth(0.0);
            return;
        }

        double progress = Math.max(
                0,
                Math.min(1, (double) remaining / KNOCK_DURATION)
        );

        BossBar bar = bossBars.get(uuid);

        if (bar != null) {
            bar.setProgress(progress);
            bar.setTitle(
                    "§cKnocked §7- §f" + (remaining / 1000) + " detik"
            );
        }

        TextDisplay display = displays.get(uuid);

        if (display != null && display.isValid()) {
            display.teleport(player.getLocation().add(0, 2.3, 0));
        }
    }

    public boolean startRevive(Player helper, Player target) {
        if (helper.equals(target) || !helper.isOnline()
                || !target.isOnline()) {
            return false;
        }

        PlayerData targetData = getData(target);

        if (targetData == null || !targetData.isKnocked()) {
            return false;
        }

        if (helper.getWorld() != target.getWorld()
                || helper.getLocation().distanceSquared(
                        target.getLocation()) > 9) {
            return false;
        }

        UUID targetId = target.getUniqueId();

        if (revives.containsKey(targetId)) {
            return false;
        }

        ReviveSession session = new ReviveSession(
                helper.getUniqueId(),
                helper.getLocation().clone()
        );

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    if (!helper.isOnline() || !target.isOnline()
                            || !targetData.isKnocked()
                            || helper.getWorld() != session.location.getWorld()
                            || helper.getLocation().distanceSquared(
                                    session.location) > 0.01
                            || helper.getWorld() != target.getWorld()
                            || helper.getLocation().distanceSquared(
                                    target.getLocation()) > 9) {
                        cancelRevive(targetId);
                        return;
                    }

                    session.ticks++;

                    if (session.ticks >= REVIVE_DURATION) {
                        completeRevive(target);
                    }
                },
                0L,
                1L
        );

        session.task = task;
        revives.put(targetId, session);

        return true;
    }

    private void completeRevive(Player target) {
        UUID uuid = target.getUniqueId();

        cancelRevive(uuid);

        PlayerData data = getData(target);

        if (data == null || !data.isKnocked()) {
            return;
        }

        data.clearKnockState();
        save(data);
        removeEffects(uuid);

        target.setHealth(Math.min(4.0, target.getMaxHealth()));

        reviveProtection.put(
                uuid,
                System.currentTimeMillis() + REVIVE_PROTECTION
        );

        target.sendMessage("§aKamu berhasil diselamatkan!");
    }

    public void cancelRevive(UUID targetId) {
        ReviveSession session = revives.remove(targetId);

        if (session != null && session.task != null) {
            session.task.cancel();
        }
    }

    public void cancelReviveForHelper(UUID helperId) {
        for (Map.Entry<UUID, ReviveSession> entry
                : new HashMap<>(revives).entrySet()) {
            if (entry.getValue().helper.equals(helperId)) {
                cancelRevive(entry.getKey());
            }
        }
    }

    public void handleQuit(Player player) {
        UUID uuid = player.getUniqueId();

        cancelRevive(uuid);
        cancelReviveForHelper(uuid);
        removeEffects(uuid);
    }

    public boolean isKnocked(Player player) {
        PlayerData data = getData(player);
        return data != null && data.isKnocked();
    }

    public boolean hasReviveProtection(Player player) {
        Long expiry = reviveProtection.get(player.getUniqueId());

        if (expiry == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expiry) {
            reviveProtection.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private PlayerData getData(Player player) {
        return plugin.getPlayerDataManager().getPlayer(
                player.getUniqueId()
        );
    }

    private void save(PlayerData data) {
        try {
            plugin.getPlayerDataManager().savePlayer(data);
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan status Knock "
                    + data.getPlayerName()
            );
            exception.printStackTrace();
        }
    }

    private void removeEffects(UUID uuid) {
        BukkitTask timer = timers.remove(uuid);

        if (timer != null) {
            timer.cancel();
        }

        BossBar bar = bossBars.remove(uuid);

        if (bar != null) {
            bar.removeAll();
        }

        TextDisplay display = displays.remove(uuid);

        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    public void shutdown() {
        for (UUID uuid : new HashMap<>(revives).keySet()) {
            cancelRevive(uuid);
        }

        for (UUID uuid : new HashMap<>(timers).keySet()) {
            removeEffects(uuid);
        }
    }

    private static class ReviveSession {
        private final UUID helper;
        private final Location location;
        private int ticks;
        private BukkitTask task;

        private ReviveSession(UUID helper, Location location) {
            this.helper = helper;
            this.location = location;
            this.ticks = 0;
        }
    }
          }
