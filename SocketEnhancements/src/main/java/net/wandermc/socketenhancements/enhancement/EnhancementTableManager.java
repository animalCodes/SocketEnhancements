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
package net.wandermc.socketenhancements.enhancement;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;
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

import static net.wandermc.socketenhancements.util.Dice.randomise;

/**
 * Manages the use of enchanting tables to enhance items. (referred to as
 * Enhancement tables")
 *
 * Note that constructing an instance of this class is sufficient to enable
 * enhancement tables.
 *
 * When an item is enhanced through an enhancement table, a random
 * enhancement from a pool corresponding to the button that was pressed will
 * be bound to the item. If `additivePools` is true, the pools will contain
 * enhancements of that rarity and lesser rarities, if it is false they will
 * only contain enhancements of that exact rarity.
 */
public class EnhancementTableManager implements Listener {
    private final JavaPlugin plugin;
    private final EnhancementManager manager;
    private final EnhancedItemForge forge;

    private final ArrayList<Enhancement> enhancementPoolI;
    private int iCounter = 0;
    private final ArrayList<Enhancement> enhancementPoolII;
    private int iiCounter = 0;
    private final ArrayList<Enhancement> enhancementPoolIII;
    private int iiiCounter = 0;

    private int randomisationFrequency;

    /**
     * Create an EnhancementTableManager for `plugin`.
     *
     * The fields to be read from `config` and their defaults are:
     * - "additive_pools": true
     * - "randomisation_frequency": 5
     *
     * @param plugin The plugin this manager is working for.
     * @param manager The current EnhancementManager.
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options for Enhancement Tables.
     */
    public EnhancementTableManager(JavaPlugin plugin,EnhancementManager manager,
        EnhancedItemForge forge, ConfigurationSection config) {
        this.plugin = plugin;
        this.manager = manager;
        this.forge = forge;

        this.randomisationFrequency = config.getInt("randomisation_frequency",
            5);

        boolean additivePools = config.getBoolean("additive_pools", true);

        enhancementPoolI = new ArrayList();
        enhancementPoolII = new ArrayList();
        enhancementPoolIII = new ArrayList();

        for (Enhancement enhancement : manager.getAll()) {
            switch (enhancement.rarity()) {
                case I: 
                    enhancementPoolI.add(enhancement);
                    if (!additivePools)
                        break;
                case II: 
                    enhancementPoolII.add(enhancement);
                    if (!additivePools)
                        break;
                case III: 
                    enhancementPoolIII.add(enhancement);
                default:
                    break;
            }
        }

        enhancementPoolI.trimToSize();
        enhancementPoolII.trimToSize();
        enhancementPoolIII.trimToSize();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Allow enchanted items with at least one empty socket to be enhanced in an
     * enchanting table.
     *
     * @param event The event.
     */
    @EventHandler
    public void handlePrepareEnchant(PrepareItemEnchantEvent event) {
        // By default, if the item placed in the enchantment table is enchanted,
        // the event will be cancelled.
        // However, we want players to be able to enhance their items regardless
        // of whether they are enchanted, so, provided the item has an empty
        // socket, allow the event to pass through.
        if (forge.create(event.getItem()).hasEmptySocket() &&
            !event.getItem().getEnchantments().isEmpty()) {
            event.setCancelled(false);
        }
    }

    /**
     * On enchanting an item with at least one empty socket, cancel the
     * enchantment and fill a socket.
     *
     * @param event The event.
     */
    @EventHandler(ignoreCancelled=true)
    public void handleEnchant(EnchantItemEvent event) {
        EnhancedItem item = forge.create(event.getItem());

        if (!item.hasEmptySocket())
            return;

        ArrayList<Enhancement> pool;
        switch (event.whichButton()) {
            case 0:
                pool = this.enhancementPoolI;
                iCounter++;
                if (iCounter > randomisationFrequency) {
                    randomise(pool);
                    iCounter = 0;
                }
                break;
            case 1:
                pool = this.enhancementPoolII;
                iiCounter++;
                if (iiCounter > randomisationFrequency) {
                    randomise(pool);
                    iiCounter = 0;
                }
                break;
            // If the player manages to find and press a 4th button, they'll get
            // the rarest pool. Good for them!
            default:
                pool = this.enhancementPoolIII;
                iiiCounter++;
                if (iiiCounter > randomisationFrequency) {
                    randomise(pool);
                    iiiCounter = 0;
                }
                break;
        }

        int i = 0;
        for (; i < pool.size(); i++) {
            if (item.bind(pool.get(i)))
                break;
        }

        if (i >= pool.size()) {
            if (!event.getItem().getEnchantments().isEmpty()) {
                event.setCancelled(true);
            }
            return;
        }

        item.update();

        event.setCancelled(true);

        if (!(event.getEnchanter().getLevel() < 1))
            event.getEnchanter().setLevel(event.getEnchanter().getLevel() - 1);
    }
}
