
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.TreeMap;

public class MobExpCommand implements CommandExecutor {

    private final AttributesRPG plugin;

    public MobExpCommand(AttributesRPG plugin) {
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

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> setExp(sender, args);
            case "get" -> getExp(sender, args);
            case "list" -> listExp(sender);
            case "remove" -> removeExp(sender, args);
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN
                        + "Konfigurasi mob EXP berhasil dimuat ulang.");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void setExp(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED
                    + "Gunakan: /mobexp set <mob> <jumlah>");
            return;
        }

        EntityType type = getEntityType(args[1]);

        if (type == null || !type.isAlive()) {
            sender.sendMessage(ChatColor.RED
                    + "Jenis mob tidak valid.");
            return;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED
                    + "Jumlah EXP harus berupa angka.");
            return;
        }

        if (amount < 0) {
            sender.sendMessage(ChatColor.RED
                    + "EXP tidak boleh negatif.");
            return;
        }

        plugin.getConfig().set(
                "mob-exp." + type.name(),
                amount
        );

        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN
                + type.name() + " sekarang memberikan "
                + amount + " EXP.");
    }

    private void getExp(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED
                    + "Gunakan: /mobexp get <mob>");
            return;
        }

        EntityType type = getEntityType(args[1]);

        if (type == null || !type.isAlive()) {
            sender.sendMessage(ChatColor.RED
                    + "Jenis mob tidak valid.");
            return;
        }

        String path = "mob-exp." + type.name();

        if (!plugin.getConfig().contains(path)) {
            sender.sendMessage(ChatColor.YELLOW
                    + type.name() + " belum memiliki pengaturan EXP.");
            return;
        }

        sender.sendMessage(ChatColor.GREEN
                + type.name() + " memberikan "
                + plugin.getConfig().getInt(path) + " EXP.");
    }

    private void listExp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD
                + "===== MOB EXP LIST =====");

        Map<String, Object> values = new TreeMap<>(
                plugin.getConfig().getConfigurationSection("mob-exp")
                        .getValues(false)
        );

        boolean found = false;

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!(entry.getValue() instanceof Number number)) {
                continue;
            }

            sender.sendMessage(ChatColor.YELLOW
                    + entry.getKey() + ": "
                    + ChatColor.GREEN + number.intValue() + " EXP");

            found = true;
        }

        if (!found) {
            sender.sendMessage(ChatColor.GRAY
                    + "Belum ada pengaturan EXP mob.");
        }
    }

    private void removeExp(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED
                    + "Gunakan: /mobexp remove <mob>");
            return;
        }

        EntityType type = getEntityType(args[1]);

        if (type == null || !type.isAlive()) {
            sender.sendMessage(ChatColor.RED
                    + "Jenis mob tidak valid.");
            return;
        }

        String path = "mob-exp." + type.name();

        if (!plugin.getConfig().contains(path)) {
            sender.sendMessage(ChatColor.RED
                    + "Mob tersebut tidak memiliki pengaturan EXP.");
            return;
        }

        plugin.getConfig().set(path, null);
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN
                + "Pengaturan EXP " + type.name()
                + " berhasil dihapus.");
    }

    private EntityType getEntityType(String name) {
        try {
            return EntityType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD
                + "===== MOB EXP COMMANDS =====");
        sender.sendMessage(ChatColor.YELLOW
                + "/mobexp set <mob> <jumlah>");
        sender.sendMessage(ChatColor.YELLOW
                + "/mobexp get <mob>");
        sender.sendMessage(ChatColor.YELLOW
                + "/mobexp list");
        sender.sendMessage(ChatColor.YELLOW
                + "/mobexp remove <mob>");
        sender.sendMessage(ChatColor.YELLOW
                + "/mobexp reload");
    }
                               }
