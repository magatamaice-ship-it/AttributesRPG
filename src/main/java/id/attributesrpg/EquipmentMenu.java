
package id.attributesrpg;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EquipmentMenu implements Listener {

    private static final String TITLE = "§8RPG EQUIPMENT";

    private final EquipmentManager manager;
    private final EquipmentItemFactory factory;
    private final EquipmentItemParser parser;

    private final int[] slots = {
            10, 12, 14, 16, 20, 22
    };

    public EquipmentMenu(
            EquipmentManager manager,
            EquipmentItemFactory factory,
            EquipmentItemParser parser
    ) {
        this.manager = manager;
        this.factory = factory;
        this.parser = parser;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(
                null, 36, TITLE
        );

        EquipmentType[] types = EquipmentType.values();

        for (int i = 0; i < types.length; i++) {
            EquipmentData data = manager.getEquipped(
                    player, types[i]
            );

            if (data == null) {
                inventory.setItem(
                        slots[i],
                        createButton(
                                Material.GRAY_DYE,
                                "§7" + types[i].getDisplayName(),
                                List.of("§8Kosong", "§eKlik untuk melihat slot")
                        )
                );
            } else {
                inventory.setItem(
                        slots[i],
                        factory.createItem(data)
                );
            }
        }

        inventory.setItem(
                31,
                createButton(
                        Material.ANVIL,
                        "§aPasang Equipment",
                        List.of(
                                "§7Pegang equipment di tangan utama",
                                "§7Lalu klik tombol ini"
                        )
                )
        );

        inventory.setItem(
                35,
                createButton(
                        Material.BARRIER,
                        "§cTutup",
                        List.of("§7Tutup menu equipment")
                )
        );

        player.openInventory(inventory);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!TITLE.equals(event.getView().getTitle())) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || event.getClickedInventory()
                != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 35) {
            player.closeInventory();
            return;
        }

        if (slot == 31) {
            equipFromHand(player);
            return;
        }

        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                unequip(player, EquipmentType.values()[i]);
                return;
            }
        }
    }

    private void equipFromHand(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().isAir() || hand.getAmount() != 1) {
            player.sendMessage(
                    "§cPegang satu item equipment di tangan utama."
            );
            return;
        }

        EquipmentData data = parser.parse(hand);

        if (data == null) {
            player.sendMessage(
                    "§cItem di tangan bukan equipment AttributesRPG."
            );
            return;
        }

        EquipmentData previous = manager.getEquipped(
                player, data.getType()
        );

        if (previous != null) {
            player.sendMessage(
                    "§cLepas equipment lama di slot tersebut terlebih dahulu."
            );
            return;
        }

        if (!manager.equip(player, data)) {
            player.sendMessage("§cEquipment gagal dipasang.");
            return;
        }

        player.getInventory().setItemInMainHand(null);
        player.sendMessage(
                "§aEquipment berhasil dipasang: §f"
                        + data.getDisplayName()
        );

        open(player);
    }

    private void unequip(Player player, EquipmentType type) {
        EquipmentData data = manager.getEquipped(player, type);

        if (data == null) {
            return;
        }

        ItemStack item = factory.createItem(data);

        manager.unequip(player, type);

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(
                    player.getLocation(), item
            );
        } else {
            player.getInventory().addItem(item);
        }

        player.sendMessage(
                "§eEquipment dilepas: §f" + data.getDisplayName()
        );

        open(player);
    }

    private ItemStack createButton(
            Material material,
            String name,
            List<String> lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }
          }
