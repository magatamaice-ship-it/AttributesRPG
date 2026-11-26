
package id.attributesrpg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkillCommand implements CommandExecutor, TabCompleter {

    private final SkillManager skillManager;
    private final SkillExecutionManager executionManager;
    private final SkillMenu skillMenu;

    public SkillCommand(
            SkillManager skillManager,
            SkillExecutionManager executionManager,
            SkillMenu skillMenu
    ) {
        this.skillManager = skillManager;
        this.executionManager = executionManager;
        this.skillMenu = skillMenu;
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
                    "Command ini hanya bisa digunakan pemain."
            );
            return true;
        }

        if (args.length == 0) {
            skillMenu.open(player);
            return true;
        }

        String action = args[0].toLowerCase();

        if (action.equals("equip")) {
            if (args.length != 2) {
                sendUsage(player);
                return true;
            }

            RpgSkill skill = RpgSkill.fromName(args[1]);

            if (skill == null) {
                player.sendMessage("§cSkill tidak ditemukan.");
                return true;
            }

            if (skillManager.equipSkill(player, skill)) {
                player.sendMessage(
                        "§aSkill §e" + skill.getDisplayName()
                                + " §aberhasil dipasang."
                );
            } else {
                player.sendMessage(
                        "§cSkill gagal dipasang. Periksa level, "
                                + "slot kosong, dan status combat."
                );
            }

            return true;
        }

        if (action.equals("unequip")) {
            if (args.length != 2) {
                sendUsage(player);
                return true;
            }

            RpgSkill skill = RpgSkill.fromName(args[1]);

            if (skill == null) {
                player.sendMessage("§cSkill tidak ditemukan.");
                return true;
            }

            if (skillManager.unequipSkill(player, skill)) {
                player.sendMessage(
                        "§aSkill §e" + skill.getDisplayName()
                                + " §aberhasil dilepas."
                );
            } else {
                player.sendMessage(
                        "§cTidak bisa melepas skill saat combat "
                                + "atau skill belum dipasang."
                );
            }

            return true;
        }

        if (args.length != 1) {
            sendUsage(player);
            return true;
        }

        try {
            int slot = Integer.parseInt(args[0]);

            if (slot < 1 || slot > 3) {
                player.sendMessage(
                        "§cGunakan slot 1 sampai 3."
                );
                return true;
            }

            executionManager.activate(player, slot);

        } catch (NumberFormatException exception) {
            sendUsage(player);
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6Perintah Skill:");
        player.sendMessage("§e/skill §7- Membuka menu skill");
        player.sendMessage("§e/skill 1-3 §7- Mengaktifkan skill");
        player.sendMessage(
                "§e/skill equip <nama_skill> §7- Memasang skill"
        );
        player.sendMessage(
                "§e/skill unequip <nama_skill> §7- Melepas skill"
        );
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!(sender instanceof Player)) {
            return List.of();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(
                    List.of("1", "2", "3", "equip", "unequip")
            );
        } else if (args.length == 2
                && (args[0].equalsIgnoreCase("equip")
                || args[0].equalsIgnoreCase("unequip"))) {

            for (RpgSkill skill : RpgSkill.values()) {
                suggestions.add(
                        skill.name().toLowerCase()
                );
            }
        }

        String input = args[args.length - 1].toLowerCase();

        return suggestions.stream()
                .filter(value -> value.startsWith(input))
                .toList();
    }
}
