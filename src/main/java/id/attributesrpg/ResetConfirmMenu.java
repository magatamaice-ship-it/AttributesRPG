
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ResetConfirmMenu implements InventoryHolder {

    private final Inventory inventory;

    public ResetConfirmMenu() {
        inventory = Bukkit.createInventory(
                this,
                27,
                ChatColor.DARK_RED + "Konfirmasi Reset Atribut"
        );

        inventory.setItem(11, createItem(
                Material.LIME_WOOL,
                ChatColor.GREEN + "KONFIRMASI RESET",
                List.of(
                        ChatColor.GRAY + "Reset seluruh atribut.",
                        ChatColor.GRAY + "Semua poin yang digunakan",
                        ChatColor.GRAY + "akan dikembalikan."
                )
        ));

        inventory.setItem(15, createItem(
                Material.RED_WOOL,
                ChatColor.RED + "BATALKAN",
                List.of(
                        ChatColor.GRAY + "Batalkan proses reset.",
                        ChatColor.GRAY + "Atribut tidak akan berubah."
                )
        ));

        ItemStack filler = createItem(
                Material.BLACK_STAINED_GLASS_PANE,
                " ",
                List.of()
        );

        for (int slot = 0; slot < 27; slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private ItemStack createItem(
            Material material,
            String name,
            List<String> lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
