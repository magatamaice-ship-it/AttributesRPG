
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
        if (!sender.hasPermission("attributesrpg.admin")) {
            sender.sendMessage("§cKamu tidak memiliki izin.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            createItem(sender, args);
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            giveItem(sender, args);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void createItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPerintah ini hanya bisa digunakan pemain.");
            return;
        }

        if (args.length < 5) {
            sender.sendMessage("§cFormat: /rpgitem create <id> <nama> <type> <rarity> [STAT=VALUE]");
            return;
        }

        try {
            String id = args[1];

            if (!id.matches("[a-zA-Z0-9_-]+")) {
                player.sendMessage("§cID hanya boleh berisi huruf, angka, _ dan -.");
                return;
            }

            String displayName = args[2].replace("_", " ");

            EquipmentType type = EquipmentType.valueOf(
                    args[3].toUpperCase(Locale.ROOT)
            );

            EquipmentRarity rarity = EquipmentRarity.valueOf(
                    args[4].toUpperCase(Locale.ROOT)
            );

            Map<EquipmentStat, Double> stats =
                    new EnumMap<>(EquipmentStat.class);

            for (int i = 5; i < args.length; i++) {
                String[] entry = args[i].split("=");

                if (entry.length != 2) {
                    player.sendMessage("§cFormat stat salah: " + args[i]);
                    return;
                }

                EquipmentStat stat = EquipmentStat.valueOf(
                        entry[0].toUpperCase(Locale.ROOT)
                );

                double value = Double.parseDouble(entry[1]);

                if (!Double.isFinite(value) || value <= 0) {
                    player.sendMessage("§cNilai stat harus lebih dari 0.");
                    return;
                }

                if (stats.putIfAbsent(stat, value) != null) {
                    player.sendMessage("§cStat tidak boleh ditulis dua kali.");
                    return;
                }
            }

            if (stats.isEmpty()) {
                player.sendMessage("§cMasukkan minimal satu stat.");
                return;
            }

            EquipmentData data = new EquipmentData(
                    id,
                    displayName,
                    type,
                    rarity,
                    stats
            );

            ItemStack item = plugin.getEquipmentItemFactory()
                    .createItem(data);

            Map<Integer, ItemStack> leftovers =
                    player.getInventory().addItem(item);

            leftovers.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(
                            player.getLocation(), leftover
                    )
            );

            player.sendMessage("§aEquipment berhasil dibuat: §f" + displayName);

        } catch (IllegalArgumentException exception) {
            player.sendMessage("§cType, rarity, atau stat tidak valid.");
        } catch (Exception exception) {
            player.sendMessage("§cGagal membuat equipment.");
            plugin.getLogger().warning(
                    "Gagal membuat equipment: " + exception.getMessage()
            );
        }
    }

    private void giveItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPerintah ini hanya bisa digunakan pemain.");
            return;
        }

        if (args.length != 2) {
            player.sendMessage("§cFormat: /rpgitem give <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            player.sendMessage("§cPemain tersebut tidak ditemukan atau offline.");
            return;
        }

        if (target.equals(player)) {
            player.sendMessage("§cKamu tidak bisa memberikan item kepada diri sendiri.");
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType() == Material.AIR
                || !plugin.getEquipmentItemFactory().isEquipment(held)) {
            player.sendMessage("§cPegang equipment RPG di tangan utama.");
            return;
        }

        ItemStack transferred = held.clone();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        Map<Integer, ItemStack> leftovers =
                target.getInventory().addItem(transferred);

        leftovers.values().forEach(leftover ->
                target.getWorld().dropItemNaturally(
                        target.getLocation(), leftover
                )
        );

        player.sendMessage("§aEquipment diberikan kepada §f" + target.getName());
        target.sendMessage("§aKamu menerima equipment dari §f" + player.getName());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6===== AttributesRPG Item =====");
        sender.sendMessage("§e/rpgitem create <id> <nama> <type> <rarity> [STAT=VALUE]");
        sender.sendMessage("§e/rpgitem give <player>");
        sender.sendMessage("§7Type: WEAPON, HELMET, CHESTPLATE, LEGGINGS, BOOTS, ACCESSORY");
        sender.sendMessage("§7Rarity: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC");
        sender.sendMessage("§7Contoh stat: STRENGTH=10 DEFENSE=5");
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("give");
            suggestions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(player ->
                    suggestions.add(player.getName())
            );
        } else if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            for (EquipmentType type : EquipmentType.values()) {
                suggestions.add(type.name());
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
            for (EquipmentRarity rarity : EquipmentRarity.values()) {
                suggestions.add(rarity.name());
            }
        } else if (args.length >= 6 && args[0].equalsIgnoreCase("create")) {
            for (EquipmentStat stat : EquipmentStat.values()) {
                suggestions.add(stat.name() + "=");
            }
        }

        String current = args[args.length - 1].toLowerCase(Locale.ROOT);

        return suggestions.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(current))
                .toList();
    }
                  }
