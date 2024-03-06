/*
 *    This file is part of SocketEnhancements: A gear enhancement plugin for PaperMC servers.
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
package net.wandermc.socketenhancements.enhancement;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.wandermc.socketenhancements.enhancement.Enhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Manages the use of enchanting tables to enhance items. (referred to as "Enhancement tables")
 * Note that constructing an instance of this class is sufficient to enable enhancement tables.
 */
public class EnhancementTableManager implements Listener {
    private final JavaPlugin plugin;
    private final EnhancementManager manager;
    private final EnhancedItemForge forge;

    // Enhancements that can be picked based on which option was selected 
    // in the enchanting table.
    // Each pool includes Enhancements of lesser rarities.
    private final ArrayList<Enhancement> enhancementPoolI;
    private final ArrayList<Enhancement> enhancementPoolII;
    private final ArrayList<Enhancement> enhancementPoolIII;

    /**
     * Create an EnhancementTableManager for `plugin`.
     *
     * @param plugin The plugin this manager is working for.
     * @param manager The current EnhancementManager.
     */
    public EnhancementTableManager(JavaPlugin plugin, EnhancementManager manager, EnhancedItemForge forge) {
        this.plugin = plugin;
        this.manager = manager;
        this.forge = forge;

        enhancementPoolI = new ArrayList();
        enhancementPoolII = new ArrayList();
        enhancementPoolIII = new ArrayList();

        // Fill out Enhancement pools with all currently registered Enhancements.
        // Enhancements will be placed in the pool of their rarity plus any lesser 
        // rarity pools.
        for (Enhancement enhancement : manager.getAll()) {
            switch (enhancement.getRarity()) {
                case I: 
                    enhancementPoolI.add(enhancement);
                case II: 
                    enhancementPoolII.add(enhancement);
                case III: 
                    enhancementPoolIII.add(enhancement);
                default:
                    break;
            }
        }

        enhancementPoolI.trimToSize();
        enhancementPoolII.trimToSize();
        enhancementPoolIII.trimToSize();

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Randomises the position of the first (size/2) items of `pool`.
     *
     * Note that it is likely the second half will be randomised as well,
     * just not guaranteed.
     *
     * @param pool The pool to shuffle.
     */
    private void shufflePool(ArrayList<Enhancement> pool) {
        int half = (int)Math.ceil(pool.size() / 2);

        Enhancement temp;
        int newIndex;
        // Iterate over first half of pool
        for (int i = 0; i <= half; i++) {
            // Create a random index in the second half of the pool
            newIndex = (int)Math.floor((Math.random() * half)) + half;
            // Swap pool[i] and pool[newIndex]
            temp = pool.get(newIndex);
            pool.set(newIndex, pool.get(i));
            pool.set(i, temp);
        }
    }

    /**
     * Allow enchanted items with at least one empty socket to be enhanced in an enchanting table.
     *
     * @param event The event.
     */
    @EventHandler
    public void handlePrepareEnchant(PrepareItemEnchantEvent event) {
        // By default, if the item placed in the enchantment table is enchanted, the event will be cancelled.
        // However, we want players to be able to enhance their items regardless of whether they are enchanted,
        // so, provided the item has an empty socket, allow the event to pass through.
        // TODO the event may have been cancelled for reasons apart from the item being enchanted, check it is enchanted as well.
        if (forge.create(event.getItem()).hasEmptySocket()) {
            event.setCancelled(false);
        }
    }

    /**
     * On enchanting an item with at least one empty socket, cancel the enchantment and fill a socket.
     *
     * @param event The event.
     */
    @EventHandler
    public void handleEnchant(EnchantItemEvent event) {
        EnhancedItem item = forge.create(event.getItem());

        if (!item.hasEmptySocket())
            return;

        // Choose pool to get enhancement from based on which button the player pressed
        ArrayList<Enhancement> pool;
        switch (event.whichButton()) {
            case 0:
                pool = this.enhancementPoolI;
                break;
            case 1:
                pool = this.enhancementPoolII;
                break;
            // If the player manages to find and press a 4th button, they'll get the 
            // rarest pool. Good for them!
            default:
                pool = this.enhancementPoolIII;
                break;
        }

        // Randomise the order of the Enhancements in pool
        // TODO figure out when to reshuffle to balance randomness and performance
        shufflePool(pool);

        // Keep picking random enhancements until we get a valid enhancement
        int i = 0;
        for (; i < pool.size(); i++) {
            if (item.bind(pool.get(i)))
                break;
        }

        if (i >= pool.size())
            // Reached the end of the pool without finding a valid enhancement,
            // return out so event continues as usual.
            return;

        item.update();

        // Stop enchantments from being added
        // (Also stops lapis and experience from being taken)
        event.setCancelled(true);

        // It is possible to have 0 levels but still use an enchanting table if you are in creative,
        // but if a player's level drops below 0 an IllegalArgumentException will be thrown.
        // .. So let's just not decrement levels if doing so would make them negative.
        if (!(event.getEnchanter().getLevel() < 1))
            event.getEnchanter().setLevel(event.getEnchanter().getLevel() - 1);
    }
}

