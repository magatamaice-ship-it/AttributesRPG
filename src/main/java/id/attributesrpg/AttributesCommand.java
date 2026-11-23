
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AttributesCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public AttributesCommand(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.GOLD
                        + "===== AttributesRPG =====");
                sender.sendMessage(ChatColor.YELLOW
                        + "/attributes - Buka menu atribut");
                sender.sendMessage(ChatColor.YELLOW
                        + "/attributes help - Bantuan");
                sender.sendMessage(ChatColor.YELLOW
                        + "/attributes reload - Muat ulang konfigurasi");
                sender.sendMessage(ChatColor.YELLOW
                        + "/attributes top - Peringkat pemain");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("attributesrpg.admin")) {
                    sender.sendMessage(ChatColor.RED
                            + "Kamu tidak memiliki izin.");
                    return true;
                }

                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN
                        + "Konfigurasi AttributesRPG dimuat ulang.");
                return true;
            }

            if (args[0].equalsIgnoreCase("top")) {
                sender.sendMessage(ChatColor.YELLOW
                        + "Leaderboard belum tersedia.");
                return true;
            }

            sender.sendMessage(ChatColor.RED
                    + "Penggunaan: /attributes [help|top|reload]");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini hanya untuk pemain.");
            return true;
        }

        new AttributeMenu(plugin).open(player);
        return true;
    }
}
