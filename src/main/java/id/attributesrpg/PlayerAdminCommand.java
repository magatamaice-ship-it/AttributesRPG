
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PlayerAdminCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public PlayerAdminCommand(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("attributesrpg.admin")) {
            sender.sendMessage(ChatColor.RED
                    + "Kamu tidak memiliki izin.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED
                    + "Gunakan: /" + label + " <player> <jumlah>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        if (!target.isOnline()) {
            sender.sendMessage(ChatColor.RED
                    + "Pemain harus sedang online.");
            return true;
        }

        Player player = target.getPlayer();

        if (player == null) {
            sender.sendMessage(ChatColor.RED
                    + "Pemain tidak ditemukan.");
            return true;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED
                    + "Jumlah harus berupa angka.");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            sender.sendMessage(ChatColor.RED
                    + "Data RPG pemain tidak ditemukan.");
            return true;
        }

        String action = command.getName().toLowerCase();

        switch (action) {
            case "setlevel" -> {
                if (amount < 0 || amount > 100) {
                    sender.sendMessage(ChatColor.RED
                            + "Level harus antara 0 sampai 100.");
                    return true;
                }

                data.setLevel(amount);
                data.setExp(0);
            }

            case "addlevel" -> {
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED
                            + "Jumlah level harus lebih dari 0.");
                    return true;
                }

                int oldLevel = data.getLevel();
                int newLevel = (int) Math.min(
                        100L,
                        (long) oldLevel + amount
                );

                int gainedLevels = newLevel - oldLevel;

                if (gainedLevels <= 0) {
                    sender.sendMessage(ChatColor.RED
                            + "Pemain sudah mencapai level maksimum.");
                    return true;
                }

                data.setLevel(newLevel);

                int pointsPerLevel = plugin.getConfig().getInt(
                        "settings.points-per-level", 1
                );

                data.setAttributePoints(
                        data.getAttributePoints()
                                + gainedLevels * pointsPerLevel
                );

                data.setExp(0);
            }

            case "addexp" -> {
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED
                            + "EXP harus lebih dari 0.");
                    return true;
                }

                plugin.getLevelManager().addExp(player, amount);
            }

            case "setexp" -> {
                if (amount < 0) {
                    sender.sendMessage(ChatColor.RED
                            + "EXP tidak boleh negatif.");
                    return true;
                }

                if (data.getLevel() >= plugin.getConfig().getInt(
                        "settings.max-player-level", 100)) {
                    sender.sendMessage(ChatColor.RED
                            + "Pemain sudah mencapai level maksimum.");
                    return true;
                }

                data.setExp(0);
                plugin.getLevelManager().addExp(player, amount);
            }

            case "givepoints" -> {
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED
                            + "Jumlah points harus lebih dari 0.");
                    return true;
                }

                data.setAttributePoints(
                        data.getAttributePoints() + amount
                );
            }

            default -> {
                sender.sendMessage(ChatColor.RED
                        + "Command tidak dikenal.");
                return true;
            }
        }

        try {
            plugin.getPlayerDataManager().savePlayer(data);
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan data RPG " + player.getName()
            );
            exception.printStackTrace();

            sender.sendMessage(ChatColor.RED
                    + "Gagal menyimpan perubahan data.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN
                + "Data RPG " + player.getName()
                + " berhasil diperbarui.");

        player.sendMessage(ChatColor.GOLD
                + "Data RPG kamu telah diperbarui oleh admin.");

        return true;
    }
}
