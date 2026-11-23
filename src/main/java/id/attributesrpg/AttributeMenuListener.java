
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.Map;

public class AttributeMenuListener implements Listener {

    private final AttributesRPG plugin;

    private static final Map<Integer, String> ATTRIBUTE_SLOTS = Map.of(
            19, "strength",
            21, "defense",
            23, "vitality",
            25, "agility",
            37, "intelligence",
            39, "luck",
            41, "endurance",
            43, "dexterity"
    );

    public AttributeMenuListener() {
        this.plugin = AttributesRPG.getInstance();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();

        if (!(inventory.getHolder() instanceof AttributeMenu)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() != inventory) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        String attribute = ATTRIBUTE_SLOTS.get(slot);

        if (attribute == null) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            player.sendMessage(ChatColor.RED
                    + "Data RPG tidak ditemukan.");
            return;
        }

        int oldLevel = data.getAttribute(attribute);

        plugin.getAttributeManager().allocatePoint(data, attribute);

        if (data.getAttribute(attribute) <= oldLevel) {
            player.sendMessage(ChatColor.RED
                    + "Attribute Points tidak cukup atau level maksimum tercapai.");
            return;
        }

        try {
            plugin.getPlayerDataManager().savePlayer(data);
        } catch (SQLException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan atribut " + player.getName()
            );
            exception.printStackTrace();

            player.sendMessage(ChatColor.RED
                    + "Gagal menyimpan atribut.");
            return;
        }

        plugin.getAttributeStatListener().applyStats(player);

        player.sendMessage(ChatColor.GREEN
                + attribute.toUpperCase()
                + " meningkat menjadi level "
                + data.getAttribute(attribute) + "!");

        player.sendMessage(ChatColor.AQUA
                + "Sisa Attribute Points: "
                + data.getAttributePoints());

        new AttributeMenu(plugin).open(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory()
                .getHolder() instanceof AttributeMenu) {
            event.setCancelled(true);
        }
    }
}
