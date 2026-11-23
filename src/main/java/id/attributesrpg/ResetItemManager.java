
package id.attributesrpg;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ResetItemManager {

    private final AttributesRPG plugin;

    public ResetItemManager(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    public boolean registerItem(Player player) {
        ItemStack heldItem = player.getInventory()
                .getItemInMainHand();

        if (heldItem.getType() == Material.AIR) {
            return false;
        }

        plugin.getConfig().set(
                "reset-item",
                heldItem.clone()
        );

        plugin.saveConfig();
        return true;
    }

    public boolean isResetItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemStack registeredItem = plugin.getConfig()
                .getItemStack("reset-item");

        if (registeredItem == null) {
            return false;
        }

        ItemStack first = item.clone();
        ItemStack second = registeredItem.clone();

        first.setAmount(1);
        second.setAmount(1);

        return first.isSimilar(second);
    }

    public ItemStack getRegisteredItem() {
        ItemStack item = plugin.getConfig()
                .getItemStack("reset-item");

        return item == null ? null : item.clone();
    }
}
