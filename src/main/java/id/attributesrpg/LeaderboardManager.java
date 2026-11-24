
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaderboardManager {

    private final AttributesRPG plugin;

    public LeaderboardManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public void showLeaderboard(CommandSender sender) {
        String sql = """
                SELECT player_name, rpg_level, exp
                FROM player_data
                ORDER BY rpg_level DESC, exp DESC, player_name ASC
                LIMIT 10
                """;

        sender.sendMessage(ChatColor.DARK_PURPLE
                + "===== TOP 10 RPG PLAYERS =====");

        try {
            Connection connection = plugin.getDatabaseManager()
                    .getConnection();

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                int position = 1;

                while (result.next()) {
                    String name = result.getString("player_name");
                    int level = result.getInt("rpg_level");
                    int exp = result.getInt("exp");

                    ChatColor color = position == 1
                            ? ChatColor.GOLD
                            : ChatColor.YELLOW;

                    sender.sendMessage(
                            color + "#" + position
                                    + " " + name
                                    + ChatColor.GRAY + " | "
                                    + ChatColor.GREEN + "Level " + level
                                    + ChatColor.GRAY + " | "
                                    + ChatColor.AQUA + exp + " EXP"
                    );

                    position++;
                }

                if (position == 1) {
                    sender.sendMessage(ChatColor.GRAY
                            + "Belum ada data pemain.");
                }
            }

        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal mengambil data leaderboard."
            );
            exception.printStackTrace();

            sender.sendMessage(ChatColor.RED
                    + "Leaderboard gagal dimuat.");
        }
    }
}
