
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

    public SkillCommand(SkillManager skillManager,
                        SkillExecutionManager executionManager) {
        this.skillManager = skillManager;
        this.executionManager = executionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Command ini hanya bisa digunakan pemain.");
            return true;
        }

        if (args.length == 0) {
            showSkills(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("equip")) {
            if (args.length < 2) {
                player.sendMessage("§cGunakan: /skill equip <nama_skill>");
                return true;
            }

            RpgSkill skill = RpgSkill.fromName(args[1]);

            if (skill == null) {
                player.sendMessage("§cSkill tidak ditemukan.");
                return true;
            }

            if (skillManager.equipSkill(player, skill)) {
                player.sendMessage("§aSkill §e" + skill.getDisplayName()
                        + " §aberhasil dipasang.");
            } else {
                player.sendMessage("§cSkill gagal dipasang. Periksa level, "
                        + "jumlah slot, dan status combat.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("unequip")) {
            if (args.length < 2) {
                player.sendMessage("§cGunakan: /skill unequip <nama_skill>");
                return true;
            }

            RpgSkill skill = RpgSkill.fromName(args[1]);

            if (skill == null) {
                player.sendMessage("§cSkill tidak ditemukan.");
                return true;
            }

            if (skillManager.unequipSkill(player, skill)) {
                player.sendMessage("§aSkill §e" + skill.getDisplayName()
                        + " §aberhasil dilepas.");
            } else {
                player.sendMessage("§cTidak bisa melepas skill saat combat "
                        + "atau skill belum dipasang.");
            }
            return true;
        }

        try {
            int slot = Integer.parseInt(args[0]);
            executionManager.activate(player, slot);
        } catch (NumberFormatException exception) {
            player.sendMessage("§cGunakan /skill, /skill equip, "
                    + "/skill unequip, atau /skill 1-3.");
        }

        return true;
    }

    private void showSkills(Player player) {
        List<RpgSkill> equipped = skillManager.getEquippedSkills(player);

        player.sendMessage("§6§l=== SKILL PLAYER ===");
        player.sendMessage("§7Skill terpasang:");

        for (int i = 0; i < SkillManager.MAX_EQUIPPED_SKILLS; i++) {
            if (i < equipped.size()) {
                RpgSkill skill = equipped.get(i);
                player.sendMessage("§e" + (i + 1) + ". §f"
                        + skill.getDisplayName()
                        + " §7(Level " + skillManager.getSkillLevel(player, skill)
                        + ")");
            } else {
                player.sendMessage("§e" + (i + 1) + ". §8Kosong");
            }
        }

        player.sendMessage("§7Gunakan §e/skill 1-3 §7untuk mengaktifkan skill.");
        player.sendMessage("§7Gunakan §e/skill equip <nama_skill> §7untuk memasang.");
        player.sendMessage("§7Gunakan §e/skill unequip <nama_skill> §7untuk melepas.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(List.of("1", "2", "3", "equip", "unequip"));
        } else if (args.length == 2
                && (args[0].equalsIgnoreCase("equip")
                || args[0].equalsIgnoreCase("unequip"))) {
            for (RpgSkill skill : RpgSkill.values()) {
                suggestions.add(skill.name().toLowerCase());
            }
        }

        String input = args[args.length - 1].toLowerCase();
        return suggestions.stream()
                .filter(value -> value.startsWith(input))
                .toList();
    }
          }
