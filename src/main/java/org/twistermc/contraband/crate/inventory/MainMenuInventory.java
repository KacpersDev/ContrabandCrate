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
import java.util.Arrays;
import java.util.Objects;

@Getter
public class MainMenuInventory implements CInventory {

    private final Main plugin;
    private Inventory inventory;

    public MainMenuInventory(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

    @Override
    public void create(Player player) {
        inventory = Bukkit.createInventory(player,
                this.plugin.getConfigConfiguration().getInt("MAIN_MENU.SIZE"),
                Color.translate(this.plugin.getConfigConfiguration().getString("MAIN_MENU.TITLE")));
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    @Override
    public void applyItems() {
        for (final String items : Objects.requireNonNull(this.plugin.getConfigConfiguration().getConfigurationSection("MAIN_MENU.ITEMS")).getKeys(false)) {
            ItemStack itemStack = new ItemStack(Material.valueOf(this.plugin.getConfigConfiguration().getString("MAIN_MENU.ITEMS." + items + ".ITEM")));
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(Color.translate(this.plugin.getConfigConfiguration().getString("MAIN_MENU.ITEMS." + items + ".NAME")));
            ArrayList<String> lore = new ArrayList<>();
            for (final String l : this.plugin.getConfigConfiguration().getStringList("MAIN_MENU.ITEMS." + items + ".LORE")) {
                lore.add(Color.translate(l));
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            for (final String enchantments : this.plugin.getConfigConfiguration().getStringList("MAIN_MENU.ITEMS." + items + ".ENCHANTMENTS")) {
                itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(enchantments.split(":")[0])), Integer.parseInt(enchantments.split(":")[1]));
            }
            itemStack.setAmount(this.plugin.getConfigConfiguration().getInt("MAIN_MENU.ITEMS." + items + ".AMOUNT"));
            inventory.setItem(this.plugin.getConfigConfiguration().getInt("MAIN_MENU.ITEMS." + items + ".SLOT"), itemStack);
        }
    }
}
