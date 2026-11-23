
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public LevelCommand(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "Command ini hanya dapat digunakan pemain."
            );
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            player.sendMessage(ChatColor.RED
                    + "Data RPG kamu belum tersedia.");
            return true;
        }

        int level = data.getLevel();
        int exp = data.getExp();
        int required = plugin.getLevelManager()
                .getRequiredExp(level);

        player.sendMessage(ChatColor.DARK_PURPLE
                + "===== RPG LEVEL =====");

        player.sendMessage(ChatColor.GOLD
                + "Level: " + ChatColor.YELLOW + level);

        if (level >= plugin.getConfig().getInt(
                "settings.max-player-level", 100)) {
            player.sendMessage(ChatColor.GREEN
                    + "Level maksimum telah tercapai!");
        } else {
            double progress = required > 0
                    ? (double) exp / required * 100
                    : 0;

            player.sendMessage(ChatColor.GREEN
                    + "EXP: " + exp + "/" + required);

            player.sendMessage(ChatColor.AQUA
                    + String.format(
                    "Progress: %.1f%%",
                    progress
            ));

            player.sendMessage(ChatColor.GRAY
                    + "Kurang " + Math.max(0, required - exp)
                    + " EXP untuk naik level.");
        }

        player.sendMessage(ChatColor.DARK_PURPLE
                + "====================");

        return true;
    }
}
