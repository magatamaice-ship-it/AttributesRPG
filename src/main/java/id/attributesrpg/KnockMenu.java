
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KnockMenu implements Listener {

    private static final String TITLE = "§8Status Knock";

    private final AttributesRPG plugin;

    public KnockMenu(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public static void open(Player player) {
        Inventory inventory = Bukkit.createInventory(
                null,
                27,
                TITLE
        );

        ItemStack waiting = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta waitingMeta = waiting.getItemMeta();

        waitingMeta.setDisplayName("§aMenunggu Bantuan");
        waitingMeta.setLore(Arrays.asList(
                "§7Kamu sedang dalam kondisi Knock.",
                "§7Pemain lain dapat membantumu.",
                "§eKlik untuk tetap menunggu."
        ));

        waiting.setItemMeta(waitingMeta);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();

        closeMeta.setDisplayName("§cTutup Menu");
        closeMeta.setLore(Arrays.asList(
                "§7Tutup menu dan tetap dalam",
                "§7kondisi Knock."
        ));

        close.setItemMeta(closeMeta);

        inventory.setItem(11, waiting);
        inventory.setItem(15, close);

        player.openInventory(inventory);
    }
  
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getRawSlot() == 11) {
            player.closeInventory();
            player.sendMessage(
                    "§aKamu tetap menunggu bantuan pemain lain."
            );
            return;
        }

        if (event.getRawSlot() == 15) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }

        if (event.getPlayer() instanceof Player player
                && !plugin.getKnockManager().isKnocked(player)) {
            return;
        }
    }
}
