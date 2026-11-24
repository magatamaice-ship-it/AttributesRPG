
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class SetAttributeCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public SetAttributeCommand(AttributesRPG plugin) {
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

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED
                    + "Gunakan: /setattribute <player> <attribute> <level>");
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

        String attribute = args[1].toLowerCase();

        if (!plugin.getAttributeManager()
                .isValidAttribute(attribute)) {
            sender.sendMessage(ChatColor.RED
                    + "Atribut tidak valid. Gunakan: strength, defense, "
                    + "vitality, agility, intelligence, luck, "
                    + "endurance, atau dexterity.");
            return true;
        }

        int level;

        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED
                    + "Level atribut harus berupa angka.");
            return true;
        }

        int maxLevel = plugin.getAttributeManager()
                .getMaxLevel(attribute);

        if (level < 0 || level > maxLevel) {
            sender.sendMessage(ChatColor.RED
                    + "Level atribut harus antara 0 sampai "
                    + maxLevel + ".");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            sender.sendMessage(ChatColor.RED
                    + "Data RPG pemain tidak ditemukan.");
            return true;
        }

        data.setAttribute(attribute, level);

        try {
            plugin.getPlayerDataManager().savePlayer(data);
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan atribut " + player.getName()
            );
            exception.printStackTrace();

            sender.sendMessage(ChatColor.RED
                    + "Gagal menyimpan perubahan atribut.");
            return true;
        }

        plugin.getAttributeStatListener().applyStats(player);

        sender.sendMessage(ChatColor.GREEN
                + "Atribut " + attribute.toUpperCase()
                + " milik " + player.getName()
                + " berhasil diatur ke level " + level + ".");

        player.sendMessage(ChatColor.GOLD
                + "Atribut " + attribute.toUpperCase()
                + " kamu diatur ke level " + level
                + " oleh admin.");

        return true;
    }
}
