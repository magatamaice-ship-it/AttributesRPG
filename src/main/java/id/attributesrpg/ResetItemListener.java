
package id.attributesrpg;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResetItemListener implements Listener {

    private final AttributesRPG plugin;

    public ResetItemListener(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (!plugin.getResetItemManager().isResetItem(item)) {
            return;
        }

        event.setCancelled(true);

        new ResetConfirmMenu().open(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();

        if (!(inventory.getHolder() instanceof ResetConfirmMenu)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)
                || event.getClickedInventory() != inventory) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 15) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW
                    + "Reset atribut dibatalkan.");
            return;
        }

        if (slot != 11) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED
                    + "Data RPG tidak ditemukan.");
            return;
        }

        int itemSlot = findResetItem(player);

        if (itemSlot == -1) {
            player.closeInventory();
            player.sendMessage(ChatColor.RED
                    + "Item reset tidak ditemukan di inventory.");
            return;
        }

        Map<String, Integer> oldAttributes =
                new LinkedHashMap<>(data.getAttributes());

        int oldPoints = data.getAttributePoints();

        plugin.getAttributeManager().resetAttributes(data);

        try {
            plugin.getPlayerDataManager().savePlayer(data);
        } catch (SQLException exception) {
            for (Map.Entry<String, Integer> entry
                    : oldAttributes.entrySet()) {
                data.setAttribute(entry.getKey(), entry.getValue());
            }

            data.setAttributePoints(oldPoints);

            plugin.getLogger().severe(
                    "Gagal menyimpan reset atribut "
                            + player.getName()
            );
            exception.printStackTrace();

            player.closeInventory();
            player.sendMessage(ChatColor.RED
                    + "Reset gagal disimpan. Item tidak dikonsumsi.");
            return;
        }

        ItemStack item = player.getInventory().getItem(itemSlot);

        if (item.getAmount() <= 1) {
            player.getInventory().setItem(itemSlot, null);
        } else {
            item.setAmount(item.getAmount() - 1);
        }

        plugin.getAttributeStatListener().applyStats(player);

        player.closeInventory();

        player.sendMessage(ChatColor.GREEN
                + "Semua atribut berhasil direset!");

        player.sendMessage(ChatColor.AQUA
                + "Attribute Points dikembalikan: "
                + data.getAttributePoints());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory()
                .getHolder() instanceof ResetConfirmMenu) {
            event.setCancelled(true);
        }
    }

    private int findResetItem(Player player) {
        ItemStack[] contents = player.getInventory().getContents();

        for (int slot = 0; slot < contents.length; slot++) {
            if (plugin.getResetItemManager()
                    .isResetItem(contents[slot])) {
                return slot;
            }
        }

        return -1;
    }
}
