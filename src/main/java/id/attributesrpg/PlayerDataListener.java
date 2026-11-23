
package id.attributesrpg;

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
        var player = event.getPlayer();

        try {
            plugin.getPlayerDataManager().loadPlayer(
                    player.getUniqueId(),
                    player.getName()
            );

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal memuat data " + player.getName()
            );
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();

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
