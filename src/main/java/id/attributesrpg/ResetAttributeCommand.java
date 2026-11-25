
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;

public class ResetAttributeCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public ResetAttributeCommand(AttributesRPG plugin) {
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
            sender.sendMessage(
                    ChatColor.RED + "Kamu tidak memiliki izin."
            );
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(
                    ChatColor.RED
                    + "Gunakan: /resetattribute <nama_pemain>"
            );
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage(
                    ChatColor.RED
                    + "Pemain tersebut harus sedang online."
            );
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(target.getUniqueId());

        if (data == null) {
            sender.sendMessage(
                    ChatColor.RED + "Data pemain belum dimuat."
            );
            return true;
        }

        Map<String, Integer> oldAttributes = data.getAttributes();
        int oldPoints = data.getAttributePoints();

        int refundedPoints =
                plugin.getAttributeManager().resetAttributes(data);

        try {
            plugin.getPlayerDataManager().savePlayer(data);

            plugin.getAttributeStatListener().applyStats(target);

            sender.sendMessage(
                    ChatColor.GREEN
                    + "Atribut " + target.getName()
                    + " berhasil di-reset."
            );

            target.sendMessage(
                    ChatColor.YELLOW
                    + "Atributmu telah di-reset oleh admin."
            );

            target.sendMessage(
                    ChatColor.GREEN
                    + "Kamu menerima kembali "
                    + refundedPoints + " Attribute Points."
            );

        } catch (SQLException exception) {
            for (Map.Entry<String, Integer> entry
                    : oldAttributes.entrySet()) {
                data.setAttribute(
                        entry.getKey(),
                        entry.getValue()
                );
            }

            data.setAttributePoints(oldPoints);

            sender.sendMessage(
                    ChatColor.RED
                    + "Reset gagal karena data tidak berhasil disimpan."
            );

            plugin.getLogger().severe(
                    "Gagal menyimpan reset atribut "
                    + target.getName()
            );

            exception.printStackTrace();
        }

        return true;
    }
}
