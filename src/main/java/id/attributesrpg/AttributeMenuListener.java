
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

        if (!(inventory.getHolder() instanceof AttributeMenu menu)) {
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

        if (!menu.isDetail()) {
            handleMainMenu(player, slot);
        } else {
            handleDetailMenu(player, menu, slot);
        }
    }

    private void handleMainMenu(Player player, int slot) {

        String attribute = ATTRIBUTE_SLOTS.get(slot);

        if (attribute == null) {
            return;
        }

        new AttributeMenu(plugin, attribute).open(player);
    }

    private void handleDetailMenu(
            Player player,
            AttributeMenu menu,
            int slot
    ) {

        if (slot == 45) {
            new AttributeMenu(plugin).open(player);
            return;
        }

        int amount;

        switch (slot) {
            case 29 -> amount = 1;
            case 31 -> amount = 5;
            case 33 -> amount = 10;
            default -> {
                return;
            }
        }

        String attribute = menu.getAttribute();

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            player.sendMessage(ChatColor.RED
                    + "Data RPG tidak ditemukan.");
            return;
        }

        int oldLevel = data.getAttribute(attribute);
        int oldPoints = data.getAttributePoints();

        int maxLevel = plugin.getAttributeManager()
                .getMaxLevel(attribute);

        if (oldPoints < amount) {
            player.sendMessage(ChatColor.RED
                    + "Attribute Points tidak cukup. Dibutuhkan "
                    + amount + " poin.");
            return;
        }

        if (oldLevel + amount > maxLevel) {
            player.sendMessage(ChatColor.RED
                    + "Level atribut maksimum adalah "
                    + maxLevel + ".");
            return;
        }

        boolean allocated = plugin.getAttributeManager()
                .allocatePoints(data, attribute, amount);

        if (!allocated) {
            player.sendMessage(ChatColor.RED
                    + "Gagal menaikkan atribut.");
            return;
        }

        try {
            plugin.getPlayerDataManager().savePlayer(data);
        } catch (SQLException exception) {

            data.setAttribute(attribute, oldLevel);
            data.setAttributePoints(oldPoints);

            plugin.getLogger().severe(
                    "Gagal menyimpan atribut pemain "
                            + player.getName()
            );

            exception.printStackTrace();

            player.sendMessage(ChatColor.RED
                    + "Gagal menyimpan atribut. Perubahan dibatalkan.");
            return;
        }

        plugin.getAttributeStatListener().applyStats(player);

        player.sendMessage(ChatColor.GREEN
                + attribute.toUpperCase()
                + " meningkat " + amount
                + " level. Level sekarang: "
                + data.getAttribute(attribute) + ".");

        player.sendMessage(ChatColor.AQUA
                + "Sisa Attribute Points: "
                + data.getAttributePoints());

        new AttributeMenu(plugin, attribute).open(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (event.getView().getTopInventory()
                .getHolder() instanceof AttributeMenu) {
            event.setCancelled(true);
        }
    }
}
