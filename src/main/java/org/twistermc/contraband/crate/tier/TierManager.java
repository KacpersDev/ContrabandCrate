package org.twistermc.contraband.crate.tier;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.twistermc.contraband.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class TierManager {

    private final Main plugin;
    public static final List<Location> tierLocations = new ArrayList<>();

    public TierManager(Main plugin) {
        this.plugin = plugin;
    }

    private void appendLocations(String tier){
        for (final String id : Objects.requireNonNull(this.plugin.getLocationConfiguration().getConfigurationSection(tier)).getKeys(false)) {
            Location location = new Location
                    (Bukkit.getWorld(Objects.requireNonNull(this.plugin.getLocationConfiguration().getString(tier + "." + id + ".world"))),
                            this.plugin.getLocationConfiguration().getInt(tier + "." + id + ".x"),
                            this.plugin.getLocationConfiguration().getInt(tier + "." + id + ".y"),
                            this.plugin.getLocationConfiguration().getInt(tier + "." + id + ".z"));
            tierLocations.add(location);
            Bukkit.broadcastMessage("Added " + location);
        }
    }

    private void removeItem(Player player){
        if (player.getInventory().getItemInMainHand().getAmount() > 1) {
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
        } else {
            player.getInventory().getItemInMainHand().setAmount(0);
        }
    }

    public void openCrate(String tier, Player player) {
        appendLocations(tier);
        removeItem(player);
        Main.running = true;
        Main.uuid = player.getUniqueId();
        Main.tier = tier;
        spawnChests();
    }

    private void spawnChests(){
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            for (final Location location : tierLocations) {
                location.getWorld().setType(location, Material.valueOf(Main.getINSTANCE().getConfigConfiguration()
                        .getString("MATERIAL")));
            }
        },this.plugin.getConfigConfiguration().getInt("PLACE-COUNTDOWN") * 20L);
    }
}
