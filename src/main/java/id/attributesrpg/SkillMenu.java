
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillMenu implements Listener {

    private static final String TITLE = "§8RPG SKILLS";

    private static final int[] SKILL_SLOTS = {
            10, 12, 14, 16,
            28, 30, 32, 34
    };

    private final AttributesRPG plugin;

    public SkillMenu(AttributesRPG plugin) {
        this.plugin = plugin;
    }

    private static class SkillMenuHolder implements InventoryHolder {

        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public void open(Player player) {
        SkillMenuHolder holder = new SkillMenuHolder();

        Inventory inventory = Bukkit.createInventory(
                holder,
                54,
                TITLE
        );

        holder.setInventory(inventory);

        SkillManager manager = plugin.getSkillManager();
        List<RpgSkill> equipped = manager.getEquippedSkills(player);

        RpgSkill[] skills = RpgSkill.values();

        for (int i = 0; i < skills.length
                && i < SKILL_SLOTS.length; i++) {

            RpgSkill skill = skills[i];

            boolean unlocked = manager.isUnlocked(player, skill);
            boolean active = equipped.contains(skill);

            Material material = active
                    ? Material.ENCHANTED_BOOK
                    : unlocked
                    ? Material.BOOK
                    : Material.GRAY_DYE;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(
                    (unlocked ? "§a" : "§c")
                            + skill.getDisplayName()
            );

            List<String> lore = new ArrayList<>();

            lore.add("§7" + skill.getDescription());
            lore.add("");
            lore.add("§7Status: "
                    + (unlocked ? "§aTerbuka" : "§cTerkunci"));

            if (unlocked) {
                lore.add("§7Level Skill: §e"
                        + manager.getSkillLevel(player, skill));
            }

            lore.add("§7Cooldown: §e"
                    + skill.getCooldownSeconds() + " detik");

            lore.add("");

            if (active) {
                lore.add("§aSkill sedang dipasang");
            } else if (unlocked) {
                lore.add("§eKlik untuk memasang skill");
            } else {
                lore.add("§cButuh atribut level 10");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(SKILL_SLOTS[i], item);
        }

        ItemStack info = new ItemStack(Material.NETHER_STAR);
        ItemMeta infoMeta = info.getItemMeta();

        infoMeta.setDisplayName("§6§lLOADOUT SKILL");

        infoMeta.setLore(List.of(
                "§7Slot maksimal: §e3",
                "§7Terpasang: §e" + equipped.size() + "/3",
                "",
                "§7Pergantian skill tidak tersedia",
                "§7selama status combat."
        ));

        info.setItemMeta(infoMeta);
        inventory.setItem(49, info);

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory()
                .getHolder() instanceof SkillMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot < 0
                || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        for (int i = 0; i < SKILL_SLOTS.length; i++) {
            if (slot != SKILL_SLOTS[i]) {
                continue;
            }

            RpgSkill[] skills = RpgSkill.values();

            if (i >= skills.length) {
                return;
            }

            RpgSkill skill = skills[i];
            SkillManager manager = plugin.getSkillManager();

            if (!manager.isUnlocked(player, skill)) {
                player.sendMessage("§cSkill belum terbuka.");
                return;
            }

            boolean success;

            if (manager.isEquipped(player, skill)) {
                success = manager.unequipSkill(player, skill);

                player.sendMessage(success
                        ? "§eSkill dilepas."
                        : "§cTidak bisa melepas skill saat combat.");
            } else {
                success = manager.equipSkill(player, skill);

                player.sendMessage(success
                        ? "§aSkill berhasil dipasang."
                        : "§cTidak bisa memasang skill. Periksa slot "
                        + "dan status combat.");
            }

            open(player);
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory()
                .getHolder() instanceof SkillMenuHolder) {
            event.setCancelled(true);
        }
    }
}
