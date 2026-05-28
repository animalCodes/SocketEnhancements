/*
 *    This file is part of SocketEnhancements: A gear enhancement plugin for
 *    PaperMC servers.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.wandermc.socketenhancements.util.cost;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A certain number of items held in the player's offhand.
 */
public class CostOffhandItem implements Cost {
    private final Material type;
    private final int amount;

    /**
     * Create a CostOffhandItem.
     *
     * @param type Material type of item.
     * @param amount Number of items.
     */
    public CostOffhandItem(Material type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    /**
     * Whether `player` has enough items held in their offhand.
     *
     * @param player The player to check.
     * @return Whether they have enough items.
     */
    public boolean met(Player player) {
        ItemStack item = player.getInventory().getItemInOffHand();
        return item.getType() == type && item.getAmount() >= amount;
    }

    /**
     * Take 'amount' items from `player`'s offhand.
     * It is assumed that the player is holding enough items of the correct type
     * in their offhand.
     *
     * @param player The player to charge.
     */
    public void take(Player player) {
        ItemStack item = player.getInventory().getItemInOffHand();
        item.setAmount(item.getAmount() - amount);
    }
}
