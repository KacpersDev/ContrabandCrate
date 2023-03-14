package org.twistermc.contraband.crate;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.twistermc.contraband.Main;
import org.twistermc.contraband.utils.Color;
import org.twistermc.contraband.utils.Config;

import java.util.ArrayList;
import java.util.Objects;

@Getter
public class CrateManager {

    private final Main plugin;

    public CrateManager(Main plugin) {
        this.plugin = plugin;
    }

    public void setCrateLocation(int id, Location crateLocation) {
        this.plugin.getDataConfiguration().set("Crate." + id + ".location", crateLocation);
        new Config(this.plugin.getData(), this.plugin.getDataConfiguration());
    }

    public int getID(){
        return this.plugin.getDataConfiguration().getInt("ID");
    }

    public void updateID(){
        this.plugin.getDataConfiguration().set("ID", (getID() + 1));
    }

    public void spawnCrate(Location location){
        location.getWorld().getBlockAt(location).setType(Material.valueOf(this.plugin.getConfigConfiguration().getString("MATERIAL")));
    }

    public void createCrate(Player player) {
        int id = getID() + 1;
        Location location = Objects.requireNonNull(player.getTargetBlock(10)).getLocation();
        setCrateLocation(id, location);
        spawnCrate(location);
        updateID();
        new Config(this.plugin.getData(), this.plugin.getDataConfiguration());
    }

    @Deprecated
    public ItemStack tierItem(String tier){
        ItemStack itemStack = new ItemStack(Material.valueOf(this.plugin.getConfigConfiguration().getString("TIER." + tier + ".ITEM")));
        NamespacedKey namespacedKey = new NamespacedKey(this.plugin, "contraband-key");
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(Color.translate(this.plugin.getConfigConfiguration().getString("TIER." + tier + ".NAME")));
        ArrayList<String> lore = new ArrayList<>();
        for (final String l : this.plugin.getConfigConfiguration().getStringList("TIER." + tier + ".LORE")) {
            lore.add(Color.translate(l));
        }
        meta.getCustomTagContainer().setCustomTag(namespacedKey, ItemTagType.STRING, tier);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        for (final String enchantments : this.plugin.getConfigConfiguration().getStringList("TIER." + tier + ".ENCHANTMENTS")) {
            itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(enchantments.split(":")[0])), Integer.parseInt(enchantments.split(":")[1]));
        }
        return itemStack;
    }

    @Deprecated
    public void giveCrate(Player target, int amount, String tier) {
        ItemStack itemStack = tierItem(tier);
        itemStack.setAmount(amount);
        target.getInventory().addItem(itemStack);
    }

    public void reload() {
    }

    @Deprecated
    public void giveAllCrate(int amount, String tier) {
        ItemStack itemStack = tierItem(tier);
        itemStack.setAmount(amount);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.getInventory().addItem(itemStack);
        }
    }
}
