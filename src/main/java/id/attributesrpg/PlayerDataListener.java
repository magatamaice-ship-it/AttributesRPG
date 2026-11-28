
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayerDataListener implements Listener {

    private final AttributesRPG plugin;

    public PlayerDataListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            plugin.getPlayerDataManager().loadPlayer(
                    player.getUniqueId(),
                    player.getName()
            );

            plugin.getSkillManager().loadPlayer(player);

            plugin.getEquipmentManager().loadPlayer(player);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                plugin.getAttributeStatListener().applyStats(player);

                plugin.getKnockManager().restoreKnock(player);
            });

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal memuat data " + player.getName()
            );

            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getKnockManager().handleQuit(player);
        plugin.getSkillManager().clearPlayer(player);
        plugin.getEquipmentManager().clearPlayer(player);

        try {
            plugin.getPlayerDataManager().unloadPlayer(
                    player.getUniqueId()
            );

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan data " + player.getName()
            );

            exception.printStackTrace();
        }
    }
}
