
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public StatsCommand(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Gunakan /stats <player>.");
                return true;
            }

            target = player;
        } else {
            target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                sender.sendMessage(ChatColor.RED
                        + "Pemain tersebut tidak sedang online.");
                return true;
            }
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(target.getUniqueId());

        if (data == null) {
            sender.sendMessage(ChatColor.RED
                    + "Data RPG pemain tidak ditemukan.");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE
                + "===== ATTRIBUTES RPG =====");

        sender.sendMessage(ChatColor.GOLD
                + "Pemain: " + target.getName());

        sender.sendMessage(ChatColor.YELLOW
                + "Level: " + data.getLevel());

        sender.sendMessage(ChatColor.GREEN
                + "EXP: " + data.getExp() + "/"
                + plugin.getLevelManager()
                .getRequiredExp(data.getLevel()));

        sender.sendMessage(ChatColor.AQUA
                + "Attribute Points: "
                + data.getAttributePoints());

        sender.sendMessage("");

        sendAttribute(sender, data, "strength", "Strength");
        sendAttribute(sender, data, "defense", "Defense");
        sendAttribute(sender, data, "vitality", "Vitality");
        sendAttribute(sender, data, "agility", "Agility");
        sendAttribute(sender, data, "intelligence", "Intelligence");
        sendAttribute(sender, data, "luck", "Luck");
        sendAttribute(sender, data, "endurance", "Endurance");
        sendAttribute(sender, data, "dexterity", "Dexterity");

        sender.sendMessage(ChatColor.DARK_PURPLE
                + "==========================");

        return true;
    }

    private void sendAttribute(
            CommandSender sender,
            PlayerData data,
            String key,
            String name
    ) {
        int level = data.getAttribute(key);
        int max = plugin.getAttributeManager().getMaxLevel(key);

        sender.sendMessage(ChatColor.LIGHT_PURPLE
                + name + ": "
                + ChatColor.WHITE + level + "/" + max);
    }
}
