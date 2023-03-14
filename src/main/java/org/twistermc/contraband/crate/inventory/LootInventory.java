package org.twistermc.contraband.crate.inventory;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.twistermc.contraband.Main;
import org.twistermc.contraband.utils.Color;
import org.twistermc.contraband.utils.inventory.CInventory;

import java.util.ArrayList;
import java.util.Objects;

@Getter
public class LootInventory implements CInventory {

    private final Main plugin;
    private final String tier;
    private final int tierID;
    private Inventory inventory;

    public LootInventory(Main plugin, String tier, int tierID){
        this.plugin = plugin;
        this.tier = tier;
        this.tierID = tierID;
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void create(Player player) {
        if (tier.equalsIgnoreCase("tier1")) {
            inventory = Bukkit.createInventory(player,
                    this.plugin.getConfigConfiguration().getInt("MENUS.TIER-1.SIZE"), Color.translate(this.plugin
                            .getConfigConfiguration().getString("MENUS.TIER-1.TITLE")));
        } else if (tier.equalsIgnoreCase("tier2")) {
            inventory = Bukkit.createInventory(player,
                    this.plugin.getConfigConfiguration().getInt("MENUS.TIER-2.SIZE"), Color.translate(this.plugin
                            .getConfigConfiguration().getString("MENUS.TIER-2.TITLE")));
        } else if (tier.equalsIgnoreCase("tier3")) {
            inventory = Bukkit.createInventory(player,
            this.plugin.getConfigConfiguration().getInt("MENUS.TIER-3.SIZE"), Color.translate(this.plugin
                    .getConfigConfiguration().getString("MENUS.TIER-3.TITLE")));
        } else if (tier.equalsIgnoreCase("tier4")) {
            inventory = Bukkit.createInventory(player,
                    this.plugin.getConfigConfiguration().getInt("MENUS.TIER-4.SIZE"), Color.translate(this.plugin
                            .getConfigConfiguration().getString("MENUS.TIER-4.TITLE")));
        }
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    @Override
    public void applyItems() {
        for (final String items : Objects.requireNonNull(this.plugin.getConfigConfiguration().getConfigurationSection("MENUS.TIER-" + tierID + ".ITEMS")).getKeys(false)) {
            ItemStack itemStack = new ItemStack(Material.valueOf(this.plugin.getConfigConfiguration().getString("MENUS.TIER-" + tierID + ".ITEMS." + items + ".ITEM")));
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(Color.translate(this.plugin.getConfigConfiguration().getString("MENUS.TIER-" + tierID + ".ITEMS." + items + ".NAME")));
            ArrayList<String> lore = new ArrayList<>();
            for (final String l : this.plugin.getConfigConfiguration().getStringList("MENUS.TIER-" + tierID + ".ITEMS." + items + ".LORE")) {
                lore.add(Color.translate(l));
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            for (final String enchantments : this.plugin.getConfigConfiguration().getStringList("MENUS.TIER-" + tierID + ".ITEMS." + items + ".ENCHANTMENTS")) {
                itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(enchantments.split(":")[0])), Integer.parseInt(enchantments.split(":")[1]));
            }
            itemStack.setAmount(this.plugin.getConfigConfiguration().getInt("MENUS.TIER-" + tierID + ".ITEMS." + items + ".AMOUNT"));
            inventory.setItem(this.plugin.getConfigConfiguration().getInt("MENUS.TIER-" + tierID + ".ITEMS." + items + ".SLOT"), itemStack);
        }
    }
}
