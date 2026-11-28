
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RpgItemCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "attributesrpg.admin";
    private static final String ID_PATTERN = "[a-zA-Z0-9_-]{2,32}";

    private final AttributesRPG plugin;

    public RpgItemCommand(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cKamu tidak memiliki izin.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> createItem(sender, args);
            case "give" -> giveItem(sender, args);
            case "list" -> listTemplates(sender);
            case "delete" -> deleteTemplate(sender, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void createItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "§cPerintah ini hanya bisa digunakan pemain."
            );
            return;
        }

        if (args.length < 6) {
            player.sendMessage(
                    "§c/rpgitem create <id> <nama> <type> <rarity> <STAT=VALUE>"
            );
            return;
        }

        try {
            String id = args[1].toLowerCase(Locale.ROOT);

            if (!id.matches(ID_PATTERN)) {
                player.sendMessage(
                        "§cID harus 2-32 karakter: huruf, angka, _ atau -."
                );
                return;
            }

            String displayName = args[2].replace("_", " ").trim();

            if (displayName.isBlank()) {
                player.sendMessage("§cNama equipment tidak boleh kosong.");
                return;
            }

            EquipmentType type = EquipmentType.valueOf(
                    args[3].toUpperCase(Locale.ROOT)
            );

            EquipmentRarity rarity = EquipmentRarity.valueOf(
                    args[4].toUpperCase(Locale.ROOT)
            );

            Map<EquipmentStat, Double> stats =
                    new EnumMap<>(EquipmentStat.class);

            for (int i = 5; i < args.length; i++) {
                String[] entry = args[i].split("=", -1);

                if (entry.length != 2
                        || entry[0].isBlank()
                        || entry[1].isBlank()) {
                    player.sendMessage(
                            "§cFormat stat salah: " + args[i]
                    );
                    return;
                }

                EquipmentStat stat = EquipmentStat.valueOf(
                        entry[0].toUpperCase(Locale.ROOT)
                );

                double value = Double.parseDouble(entry[1]);

                if (!Double.isFinite(value) || value <= 0) {
                    player.sendMessage(
                            "§cNilai stat harus lebih dari 0."
                    );
                    return;
                }

                if (stats.putIfAbsent(stat, value) != null) {
                    player.sendMessage(
                            "§cStat tidak boleh ditulis dua kali."
                    );
                    return;
                }
            }

            EquipmentData data = new EquipmentData(
                    id,
                    displayName,
                    type,
                    rarity,
                    stats
            );

            boolean saved = plugin.getEquipmentTemplateManager()
                    .saveTemplate(data);

            if (!saved) {
                player.sendMessage(
                        "§cTemplate gagal disimpan. ID mungkin sudah digunakan atau statistik melewati batas."
                );
                return;
            }

            ItemStack item = plugin.getEquipmentItemFactory()
                    .createItem(data);

            giveStack(player, item);

            player.sendMessage(
                    "§aTemplate berhasil disimpan: §f" + id
            );

        } catch (IllegalArgumentException exception) {
            player.sendMessage(
                    "§cType, rarity, atau stat tidak valid."
            );
        } catch (Exception exception) {
            player.sendMessage("§cGagal membuat equipment.");

            plugin.getLogger().warning(
                    "Gagal membuat equipment: "
                            + exception.getMessage()
            );
        }
    }

    private void giveItem(CommandSender sender, String[] args) {
        if (args.length != 2 && args.length != 3) {
            sender.sendMessage("§c/rpgitem give <player> [id]");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(
                    "§cPemain tidak ditemukan atau offline."
            );
            return;
        }

        if (args.length == 3) {
            EquipmentData data = plugin.getEquipmentTemplateManager()
                    .getTemplate(args[2]);

            if (data == null) {
                sender.sendMessage(
                        "§cTemplate tidak ditemukan: " + args[2]
                );
                return;
            }

            try {
                ItemStack item = plugin.getEquipmentItemFactory()
                        .createItem(data);

                giveStack(target, item);

                sender.sendMessage(
                        "§aEquipment §f" + data.getDisplayName()
                                + " §atelah diberikan kepada §f"
                                + target.getName()
                );

                target.sendMessage(
                        "§aKamu menerima equipment: §f"
                                + data.getDisplayName()
                );

            } catch (Exception exception) {
                sender.sendMessage(
                        "§cGagal membuat item equipment."
                );

                plugin.getLogger().warning(
                        "Gagal memberikan equipment: "
                                + exception.getMessage()
                );
            }

            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "§cConsole harus menggunakan ID template."
            );
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(
                    "§cTidak bisa memberikan item kepada diri sendiri."
            );
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType() == Material.AIR
                || !plugin.getEquipmentItemFactory().isEquipment(held)) {
            player.sendMessage(
                    "§cPegang equipment RPG di tangan utama."
            );
            return;
        }

        ItemStack transferred = held.clone();

        giveStack(target, transferred);

        player.getInventory().setItemInMainHand(
                new ItemStack(Material.AIR)
        );

        player.sendMessage(
                "§aEquipment diberikan kepada " + target.getName()
        );

        target.sendMessage(
                "§aKamu menerima equipment dari " + player.getName()
        );
    }

    private void giveStack(Player target, ItemStack item) {
        if (target == null || item == null
                || item.getType().isAir()) {
            return;
        }

        Map<Integer, ItemStack> leftovers =
                target.getInventory().addItem(item);

        leftovers.values().forEach(leftover ->
                target.getWorld().dropItemNaturally(
                        target.getLocation(), leftover
                )
        );
    }

    private void listTemplates(CommandSender sender) {
        List<EquipmentData> templates =
                plugin.getEquipmentTemplateManager().getTemplates();

        if (templates.isEmpty()) {
            sender.sendMessage(
                    "§eBelum ada template equipment."
            );
            return;
        }

        sender.sendMessage(
                "§6===== Equipment Templates ====="
        );

        for (EquipmentData data : templates) {
            sender.sendMessage(
                    "§e" + data.getId()
                            + " §7- §f" + data.getDisplayName()
                            + " §7(" + data.getType().name()
                            + ", " + data.getRarity().name() + ")"
            );
        }
    }

    private void deleteTemplate(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§c/rpgitem delete <id>");
            return;
        }

        boolean deleted = plugin.getEquipmentTemplateManager()
                .deleteTemplate(args[1]);

        if (deleted) {
            sender.sendMessage(
                    "§aTemplate berhasil dihapus."
            );
        } else {
            sender.sendMessage(
                    "§cTemplate tidak ditemukan atau gagal dihapus."
            );
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(
                "§6===== AttributesRPG Item ====="
        );
        sender.sendMessage(
                "§e/rpgitem create <id> <nama> <type> <rarity> <STAT=VALUE>"
        );
        sender.sendMessage(
                "§e/rpgitem give <player> <id>"
        );
        sender.sendMessage(
                "§e/rpgitem give <player> §7- pindahkan item yang dipegang"
        );
        sender.sendMessage("§e/rpgitem list");
        sender.sendMessage("§e/rpgitem delete <id>");
        sender.sendMessage("§e/rpgitem help");
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(List.of(
                    "create", "give", "list", "delete", "help"
            ));
        } else if (args.length == 2
                && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(player ->
                    suggestions.add(player.getName())
            );
        } else if (args.length == 2
                && args[0].equalsIgnoreCase("delete")) {
            plugin.getEquipmentTemplateManager()
                    .getTemplates()
                    .forEach(data ->
                            suggestions.add(data.getId())
                    );
        } else if (args.length == 3
                && args[0].equalsIgnoreCase("give")) {
            plugin.getEquipmentTemplateManager()
                    .getTemplates()
                    .forEach(data ->
                            suggestions.add(data.getId())
                    );
        } else if (args.length == 4
                && args[0].equalsIgnoreCase("create")) {
            for (EquipmentType type : EquipmentType.values()) {
                suggestions.add(type.name());
            }
        } else if (args.length == 5
                && args[0].equalsIgnoreCase("create")) {
            for (EquipmentRarity rarity : EquipmentRarity.values()) {
                suggestions.add(rarity.name());
            }
        } else if (args.length >= 6
                && args[0].equalsIgnoreCase("create")) {
            for (EquipmentStat stat : EquipmentStat.values()) {
                suggestions.add(stat.name() + "=");
            }
        }

        String current = args[args.length - 1]
                .toLowerCase(Locale.ROOT);

        return suggestions.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT)
                        .startsWith(current))
                .toList();
    }
}
