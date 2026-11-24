
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
    private final String attribute;

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

    private static final Map<String, String> BONUS_PATHS = Map.of(
            "strength", "attributes.strength.damage-per-level",
            "defense", "attributes.defense.reduction-per-level",
            "vitality", "attributes.vitality.health-per-level",
            "agility", "attributes.agility.speed-per-level",
            "intelligence", "attributes.intelligence.magic-damage-per-level",
            "luck", "attributes.luck.loot-chance-per-level",
            "endurance", "attributes.endurance.effect-resistance-per-level",
            "dexterity", "attributes.dexterity.ranged-damage-per-level"
    );

    private static final Map<String, String> BONUS_UNITS = Map.of(
            "strength", "% melee damage",
            "defense", "% damage reduction",
            "vitality", " HP",
            "agility", "% movement speed",
            "intelligence", "% magic damage",
            "luck", "% loot chance",
            "endurance", "% effect resistance",
            "dexterity", "% ranged damage"
    );

    public AttributeMenu(AttributesRPG plugin) {
        this(plugin, null);
    }

    public AttributeMenu(AttributesRPG plugin, String attribute) {
        this.plugin = plugin;

        if (attribute != null
                && plugin.getAttributeManager().isValidAttribute(attribute)) {
            this.attribute = attribute.toLowerCase();
        } else {
            this.attribute = null;
        }

        String title = this.attribute == null
                ? "&5&lATTRIBUTES RPG"
                : "&5&l" + NAMES.get(this.attribute);

        this.inventory = Bukkit.createInventory(
                this,
                54,
                color(title)
        );
    }

    public void open(Player player) {
        PlayerData data = plugin.getPlayerDataManager()
                .getPlayer(player.getUniqueId());

        if (data == null) {
            player.sendMessage(color("&cData RPG tidak ditemukan."));
            return;
        }

        inventory.clear();

        if (attribute == null) {
            buildMainMenu(data);
        } else {
            buildDetailMenu(data);
        }

        player.openInventory(inventory);
    }

    public boolean isDetail() {
        return attribute != null;
    }

    public String getAttribute() {
        return attribute;
    }

    private void buildMainMenu(PlayerData data) {
        fillBackground();

        List<String> profileLore = new ArrayList<>();
        profileLore.add(color("&7Level: &e" + data.getLevel()));
        profileLore.add(color("&7EXP: &e" + data.getExp()));
        profileLore.add(color("&7Attribute Points: &a"
                + data.getAttributePoints()));

        inventory.setItem(4, createItem(
                Material.NETHER_STAR,
                "&6&lPROFIL RPG",
                profileLore
        ));

        int[] slots = {19, 21, 23, 25, 37, 39, 41, 43};

        String[] attributes = {
                "strength", "defense", "vitality", "agility",
                "intelligence", "luck", "endurance", "dexterity"
        };

        for (int i = 0; i < attributes.length; i++) {
            String key = attributes[i];

            inventory.setItem(slots[i], createAttributeItem(data, key));
        }

        inventory.setItem(49, createItem(
                Material.BARRIER,
                "&c&lTUTUP",
                "&7Klik untuk menutup menu."
        ));
    }

    private void buildDetailMenu(PlayerData data) {
        fillBackground();

        int level = data.getAttribute(attribute);
        int max = plugin.getAttributeManager().getMaxLevel(attribute);

        double perLevel = plugin.getConfig().getDouble(
                BONUS_PATHS.get(attribute),
                0.0
        );

        double currentBonus = level * perLevel;
        double maximumBonus = max * perLevel;

        if (!attribute.equals("vitality")) {
            currentBonus *= 100;
            maximumBonus *= 100;
        }

        List<String> lore = new ArrayList<>();
        lore.add(color("&7Level: &e" + level + "&7/&e" + max));
        lore.add(color("&7Bonus saat ini: &a"
                + formatBonus(currentBonus)
                + BONUS_UNITS.get(attribute)));
        lore.add(color("&7Bonus maksimum: &6"
                + formatBonus(maximumBonus)
                + BONUS_UNITS.get(attribute)));
        lore.add("");
        lore.add(color("&7Attribute Points: &a"
                + data.getAttributePoints()));

        inventory.setItem(22, createItem(
                ICONS.get(attribute),
                "&6&l" + NAMES.get(attribute),
                lore
        ));

        inventory.setItem(29, createItem(
                Material.LIME_DYE,
                "&a&lNAIKKAN +1",
                "&7Tambahkan 1 level."
        ));

        inventory.setItem(31, createItem(
                Material.LIME_DYE,
                "&a&lNAIKKAN +5",
                "&7Tambahkan 5 level."
        ));

        inventory.setItem(33, createItem(
                Material.LIME_DYE,
                "&a&lNAIKKAN +10",
                "&7Tambahkan 10 level."
        ));

        inventory.setItem(45, createItem(
                Material.ARROW,
                "&e&lKEMBALI",
                "&7Kembali ke menu atribut."
        ));

        inventory.setItem(49, createItem(
                Material.BARRIER,
                "&c&lTUTUP",
                "&7Klik untuk menutup menu."
        ));
    }

    private ItemStack createAttributeItem(
            PlayerData data,
            String key
    ) {
        int level = data.getAttribute(key);
        int max = plugin.getAttributeManager().getMaxLevel(key);

        return createItem(
                ICONS.get(key),
                "&6&l" + NAMES.get(key),
                "&7Level: &e" + level + "&7/&e" + max,
                "",
                "&eKlik untuk melihat detail."
        );
    }

    private void fillBackground() {
        ItemStack pane = createItem(
                Material.PURPLE_STAINED_GLASS_PANE,
                " ",
                ""
        );

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, pane);
        }
    }

    private ItemStack createItem(
            Material material,
            String name,
            String... lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(name));

            List<String> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(color(line));
            }

            meta.setLore(lines);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createItem(
            Material material,
            String name,
            List<String> lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color(name));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String formatBonus(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
