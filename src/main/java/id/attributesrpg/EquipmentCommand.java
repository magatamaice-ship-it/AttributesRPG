
package id.attributesrpg;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class EquipmentCommand implements CommandExecutor, TabCompleter {

    private final EquipmentMenu menu;

    public EquipmentCommand(EquipmentMenu menu) {
        this.menu = menu;
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

        if (args.length != 0) {
            player.sendMessage("§cGunakan /equipment");
            return true;
        }

        menu.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        return Collections.emptyList();
    }
}
