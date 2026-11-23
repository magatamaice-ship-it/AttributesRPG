
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetItemCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public SetItemCommand(AttributesRPG plugin) {
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

        if (!player.hasPermission("attributesrpg.admin")) {
            player.sendMessage(ChatColor.RED
                    + "Kamu tidak memiliki izin.");
            return true;
        }

        if (!plugin.getResetItemManager().registerItem(player)) {
            player.sendMessage(ChatColor.RED
                    + "Pegang item terlebih dahulu.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN
                + "Item reset atribut berhasil didaftarkan!");
        player.sendMessage(ChatColor.GRAY
                + "Pemain dapat menggunakan item ini untuk membuka menu reset.");

        return true;
    }
}
