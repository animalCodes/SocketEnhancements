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
import net.wandermc.socketenhancements.gear.EnhancedItem;

/**
 * Manages the use of enchanting tables to enhance items. (referred to as "Enhancement tables")
 * Note that constructing an instance of this class is sufficient to enable enhancement tables.
 */
public class EnhancementTableManager implements Listener {
    private final JavaPlugin plugin;
    private final EnhancementManager manager;

    private final ArrayList<Enhancement> enhancements;

    /**
     * Create an EnhancementTableManager for `plugin`.
     *
     * @param plugin The plugin this manager is working for.
     * @param manager The current EnhancementManager.
     */
    public EnhancementTableManager(JavaPlugin plugin, EnhancementManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        this.enhancements = new ArrayList<Enhancement>(manager.getAll());

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Pick a random enhancement from `this.enhancements`.
     *
     * @return The Enhancement.
     */
    private Enhancement pickRandomEnhancement() {
        double random = Math.random();
        random = (random == 0 ? 0.9 : random); // Avoid division by zero
        return enhancements.get(
            (int)Math.floor(random * enhancements.size()));
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
        if (new EnhancedItem(manager, event.getItem()).hasEmptySocket()) {
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
        EnhancedItem item = new EnhancedItem(manager, event.getItem());

        if (!item.hasEmptySocket())
            return;

        // Keep picking random enhancements until we get a valid enhancement.
        Enhancement enhancement = pickRandomEnhancement();
        while (!item.bind(enhancement))
            enhancement = pickRandomEnhancement();

        item.update();

        // Stop enchantments from being added
        // (Also stops lapis and experience from being taken)
        event.setCancelled(true);

        // It is possible to have 0 levels but still use an enchanting table if you are in creative,
        // but if a player's level drops below 0 an IllegalArgumentException will be thrown.
        // .. So let's just not decrement levels if doing so would make them negative.
        if (event.getEnchanter().getLevel() < 1)
            event.getEnchanter().setLevel(event.getEnchanter().getLevel() - 1);
    }
}

