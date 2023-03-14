package org.twistermc.contraband.crate.listener;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.twistermc.contraband.Main;
import org.twistermc.contraband.crate.CrateManager;
import org.twistermc.contraband.crate.inventory.LootInventory;
import org.twistermc.contraband.crate.inventory.MainMenuInventory;
import org.twistermc.contraband.crate.tier.TierManager;
import org.twistermc.contraband.utils.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Getter
public class ContrabandListener implements Listener {

    private final Main plugin;
    private final TierManager tierManager;
    private final CrateManager crateManager;
    private final MainMenuInventory mainMenuInventory;
    private final Random random = new Random();

    public ContrabandListener(Main plugin) {
        this.plugin = plugin;
        this.tierManager = new TierManager(this.plugin);
        this.crateManager = new CrateManager(this.plugin);
        this.mainMenuInventory = new MainMenuInventory(this.plugin);
    }

    @EventHandler
    @Deprecated
    public void onInteract(PlayerInteractEvent event) {
        NamespacedKey namespacedKey = new NamespacedKey(this.plugin, "contraband-key");
        if (event.getClickedBlock() == null) return;
        if (validateLocation(Objects.requireNonNull(event.getClickedBlock()).getLocation())) {
            event.setCancelled(true);
        }
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getPlayer().getInventory().getItemInMainHand() == null
        || event.getPlayer().getInventory().getItemInMainHand().getItemMeta() == null
        || event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName() == null) return;
        if (!event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getCustomTagContainer()
                .hasCustomTag(namespacedKey, ItemTagType.STRING)) return;
        if (!validateLocation(Objects.requireNonNull(event.getClickedBlock()).getLocation())) return;
        if (!isAvailable()) return;
        this.tierManager.openCrate(event.getPlayer().getInventory().getItemInMainHand()
                .getItemMeta().getCustomTagContainer().getCustomTag(namespacedKey, ItemTagType.STRING), event.getPlayer());
        event.getClickedBlock().getLocation().getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.valueOf
                (this.plugin.getConfigConfiguration().getString("SPAWN-EFFECT")), 1);
    }

    @EventHandler
    public void onLoot(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (this.plugin.getDataConfiguration().getConfigurationSection("Crate") == null) {
                return;
            }
            for (final String id : Objects.requireNonNull(this.plugin.getDataConfiguration().getConfigurationSection("Crate")).getKeys(false)) {
                Location location = this.plugin.getDataConfiguration().getLocation("Crate." + id + ".location");
                if (Objects.requireNonNull(location).getX() == Objects.requireNonNull(event.getClickedBlock()).getLocation().getX()
                && location.getY() == event.getClickedBlock().getLocation().getY()
                && location.getZ() == event.getClickedBlock().getLocation().getZ()) {
                    event.setCancelled(true);
                    this.mainMenuInventory.create(event.getPlayer());
                    this.mainMenuInventory.applyItems();
                    this.mainMenuInventory.open(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(Color.translate(this.plugin.getConfigConfiguration()
                .getString("MAIN_MENU.TITLE")))) event.setCancelled(true);
        for (final String items : Objects.requireNonNull(this.plugin.getConfigConfiguration().getConfigurationSection("MAIN_MENU.ITEMS")).getKeys(false)) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null || event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            if (Objects.requireNonNull(event.getCurrentItem()).getItemMeta().getDisplayName().equalsIgnoreCase(Color.translate(this.plugin.getConfigConfiguration()
                    .getString("MAIN_MENU.ITEMS." + items + ".NAME")))) {
                LootInventory lootInventory = new LootInventory(this.plugin, ("tier" + this.plugin.getConfigConfiguration().getInt("MAIN_MENU.ITEMS." + items + ".TIER")), this.plugin.getConfigConfiguration().getInt("MAIN_MENU.ITEMS." + items + ".TIER"));
                lootInventory.create((Player) event.getWhoClicked());
                lootInventory.applyItems();
                lootInventory.open((Player) event.getWhoClicked());
            }
        }
    }

    @EventHandler
    @Deprecated
    public void onQuit(PlayerQuitEvent event){
        if (Main.uuid != null && Main.uuid.equals(event.getPlayer().getUniqueId())) {
            ItemStack itemStack = this.crateManager.tierItem(Main.tier);
            itemStack.setAmount(Main.amount);
            NamespacedKey namespacedKey = new NamespacedKey(this.plugin, "contraband-crate");
            ItemMeta meta = itemStack.getItemMeta();
            meta.getCustomTagContainer().setCustomTag(namespacedKey, ItemTagType.STRING, Main.tier);
            itemStack.setItemMeta(meta);
            event.getPlayer().getInventory().addItem(itemStack);
            Main.reset();
        }
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent event) {
        if (Main.running && Main.uuid == event.getPlayer().getUniqueId()) {
            if (Main.allowedAmount > 0) {
                if (TierManager.tierLocations != null && TierManager.tierLocations.size() > 0) {
                    for (Location location : TierManager.tierLocations) {
                        if (Objects.requireNonNull(event.getClickedBlock()).getLocation().equals(location)) {
                            event.setCancelled(true);
                            event.getClickedBlock().setType(Material.AIR);
                            loot(event.getPlayer());

                            if (TierManager.tierLocations.size() == 1) {
                                TierManager.tierLocations.clear();
                                Main.reset();
                            }
                            TierManager.tierLocations.remove(location);
                            Main.allowedAmount = Main.allowedAmount - 1;
                        }
                    }
                }
            } else {
                event.getPlayer().sendMessage("Cannot open more");
                for (Location location : TierManager.tierLocations) {
                    location.getWorld().setType(location, Material.AIR);
                }
                TierManager.tierLocations.clear();
                Main.reset();
            }
        }
    }

    public boolean validateLocation(Location location){
        if (this.plugin.getDataConfiguration().getConfigurationSection("Crate") == null) return false;
        for (final String id : Objects.requireNonNull(this.plugin.getDataConfiguration().getConfigurationSection("Crate")).getKeys(false)) {
            Location idLocation = this.plugin.getDataConfiguration().getLocation("Crate." + id + ".location");
            if (location.equals(idLocation)) {
                return true;
            }
        }

        return false;
    }

    private void loot(Player player){

        List<ItemStack> i = new ArrayList<>();
        int randomNumber = random.nextInt(100);

        for (final String items : Objects.requireNonNull(this.plugin.getConfigConfiguration().getConfigurationSection("LOOT-" + Main.tier)).getKeys(false)) {
            ItemStack itemStack = new ItemStack(Material.valueOf(this.plugin.getConfigConfiguration().
                    getString("LOOT-" + Main.tier + "." + items + ".ITEM")));
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(Color.translate(this.plugin.getConfigConfiguration().getString
                    ("LOOT-" + Main.tier + "." + items + ".NAME")));
            ArrayList<String> lore = new ArrayList<>();
            for (final String l : this.plugin.getConfigConfiguration().getStringList("LOOT-" + Main.tier + "." + items + ".LORE")) {
                lore.add(Color.translate(l));
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            for (final String enchantments : this.plugin.getConfigConfiguration().getStringList("LOOT-" + Main.tier + "." + items + ".ENCHANTMENTS")) {
                itemStack.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByName(enchantments.split(":")[0])), Integer.parseInt(enchantments.split(":")[1]));
            }

            if (this.plugin.getConfigConfiguration().getBoolean("LOOT-" + Main.tier + "." + items + ".ITEM-ENABLED")) {
                itemStack.setAmount(this.plugin.getConfigConfiguration().getInt("LOOT-" + Main.tier + "." + items + ".AMOUNT"));
                if (randomNumber < (this.plugin.getConfigConfiguration().getInt("LOOT-" + Main.tier + "." + items + ".CHANCE"))) {
                    i.add(itemStack);
                }
            }
        }
        if (i.size() > 0) {
            ItemStack reward = i.get(random.nextInt(i.size()));
            player.getInventory().addItem(reward);
        }
    }

    private boolean isAvailable(){
        return !Main.running;
    }
}
