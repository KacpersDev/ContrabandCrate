package org.twistermc.contraband.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface CInventory {

    void open(Player player);
    void create(Player player);
    void close(Player player);
    void applyItems();
}
