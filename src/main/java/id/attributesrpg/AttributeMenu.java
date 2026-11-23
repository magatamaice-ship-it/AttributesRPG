
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeMenu implements InventoryHolder {

    private final AttributesRPG plugin;
    private final Inventory inventory;

    private static final Map<String, Material> ICONS = Map.of(
            "strength", Material.IRON_SWORD,
            "defense", Material.SHIELD,
            "vitality", Material.GOLDEN_APPLE,
            "agility", Material.FEATHER,
            "intelligence", Material.ENCHANTED_BOOK,
            "luck", Material.EMERALD,
            "endurance", Material.TURTLE_HELMET,
            "dexterity", Material.BOW
    );

    private static final Map<String, String> NAMES = Map.of(
            "strength", "Strength",
            "defense", "Defense",
            "vitality", "Vitality",
            "agility", "Agility",
            "intelligence", "Intelligence",
            "luck", "Luck",
            "endurance", "Endurance",
            "dexterity", "Dexterity"
    );

    public AttributeMenu(AttributesRPG plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(
                this,
                54,
                ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
                        + "ATTRIBUTES RPG"
        );
    }

    public void open(Player player) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            player.sendMessage(ChatColor.RED
                    + "Data RPG kamu belum siap.");
            return;
        }

        inventory.clear();

        ItemStack filler = createItem(
                Material.PURPLE_STAINED_GLASS_PANE,
                " ",
                List.of()
        );

        for (int slot = 0; slot < 54; slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(4, createItem(
                Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "Profil RPG",
                List.of(
                        ChatColor.GRAY + "Level: "
                                + ChatColor.YELLOW + data.getLevel(),
                        ChatColor.GRAY + "EXP: "
                                + ChatColor.GREEN + data.getExp()
                                + "/" + plugin.getLevelManager()
                                .getRequiredExp(data.getLevel()),
                        ChatColor.GRAY + "Attribute Points: "
                                + ChatColor.AQUA
                                + data.getAttributePoints()
                )
        ));

        String[] attributes = {
                "strength", "defense",
                "vitality", "agility",
                "intelligence", "luck",
                "endurance", "dexterity"
        };

        int[] slots = {
                19, 21, 23, 25,
                37, 39, 41, 43
        };

        for (int i = 0; i < attributes.length; i++) {
            String attribute = attributes[i];

            int level = data.getAttribute(attribute);
            int maxLevel = plugin.getAttributeManager()
                    .getMaxLevel(attribute);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Level: "
                    + ChatColor.YELLOW + level + "/" + maxLevel);
            lore.add("");

            if (level >= maxLevel) {
                lore.add(ChatColor.RED + "Level maksimum tercapai.");
            } else if (data.getAttributePoints() > 0) {
                lore.add(ChatColor.GREEN + "Klik untuk meningkatkan.");
                lore.add(ChatColor.GRAY + "Biaya: "
                        + ChatColor.AQUA + "1 Attribute Point");
            } else {
                lore.add(ChatColor.RED + "Attribute Points tidak cukup.");
            }

            inventory.setItem(slots[i], createItem(
                    ICONS.get(attribute),
                    ChatColor.LIGHT_PURPLE + NAMES.get(attribute),
                    lore
            ));
        }

        inventory.setItem(49, createItem(
                Material.BARRIER,
                ChatColor.RED + "Tutup Menu",
                List.of(ChatColor.GRAY + "Klik untuk menutup menu.")
        ));

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
            meta.setLore(new ArrayList<>(lore));
            item.setItemMeta(meta);
        }

        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
