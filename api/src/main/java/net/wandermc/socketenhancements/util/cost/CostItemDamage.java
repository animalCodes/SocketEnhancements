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

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

/**
 * Damage applied to an item.
 * Will not break the item.
 * Behaviour for none-damageable items can be set.
 */
public class CostItemDamage implements Cost<ItemStack> {
    private final int damage;
    private final boolean allowUnbreakable;

    /**
     * Create a CostItemDamage.
     *
     * @param damage Amount to damage item by.
     * @param allowUnbreakable Whether to allow items that cannot be damaged.
     */
    public CostItemDamage(int damage, boolean allowUnbreakable) {
        this.damage = damage;
        this.allowUnbreakable = allowUnbreakable;
    }

    /**
     * Whether `item` can receive set damage without breaking.
     * Returns false if there is no item in mainhand, how unbreakable items are
     * treated is determined by the value of 'allowUnbreakable'.
     *
     * @param item The item to check.
     * @return Whether it is suitable.
     */
    public boolean met(ItemStack item) {
        if (item.isEmpty())
            return false;

        if (item.getItemMeta() instanceof Damageable damageable) {
            return item.getType().getMaxDurability() - damageable.getDamage() >
                damage;
        } else if (allowUnbreakable) {
            return true;
        }

        return false;
    }

    /**
     * Damage `item` by given amount.
     *
     * @param item The item to damage.
     */
    public void take(ItemStack item) {
        Damageable damageable = (Damageable)item.getItemMeta();
        damageable.setDamage(damageable.getDamage() + damage);
        item.setItemMeta(damageable);
    }
}
