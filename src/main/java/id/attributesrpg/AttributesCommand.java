
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AttributesCommand implements CommandExecutor {

    private final AttributesRPG plugin;
    private final LeaderboardManager leaderboardManager;

    public AttributesCommand(AttributesRPG plugin) {
        this.plugin = plugin;
        this.leaderboardManager = new LeaderboardManager(plugin);
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "top" -> {
                    leaderboardManager.showLeaderboard(sender);
                    return true;
                }

                case "help" -> {
                    sendHelp(sender);
                    return true;
                }

                case "reload" -> {
                    if (!sender.hasPermission("attributesrpg.admin")) {
                        sender.sendMessage(ChatColor.RED
                                + "Kamu tidak memiliki izin.");
                        return true;
                    }

                    plugin.reloadConfig();

                    sender.sendMessage(ChatColor.GREEN
                            + "Konfigurasi AttributesRPG berhasil dimuat ulang.");
                    return true;
                }

                default -> {
                    sendHelp(sender);
                    return true;
                }
            }
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED
                    + "Command ini hanya bisa digunakan oleh pemain.");
            return true;
        }

        new AttributeMenu(plugin).open(player);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD
                + "===== ATTRIBUTES RPG HELP =====");

        sender.sendMessage(ChatColor.YELLOW
                + "/attributes"
                + ChatColor.GRAY + " - Membuka menu atribut");

        sender.sendMessage(ChatColor.YELLOW
                + "/attributes top"
                + ChatColor.GRAY + " - Melihat Top 10 pemain");

        sender.sendMessage(ChatColor.YELLOW
                + "/attributes help"
                + ChatColor.GRAY + " - Melihat bantuan");

        if (sender.hasPermission("attributesrpg.admin")) {
            sender.sendMessage(ChatColor.YELLOW
                    + "/attributes reload"
                    + ChatColor.GRAY + " - Memuat ulang konfigurasi");
        }

        sender.sendMessage(ChatColor.YELLOW
                + "/stats [player]"
                + ChatColor.GRAY + " - Melihat statistik");

        sender.sendMessage(ChatColor.YELLOW
                + "/level"
                + ChatColor.GRAY + " - Melihat level dan EXP");
    }
}
