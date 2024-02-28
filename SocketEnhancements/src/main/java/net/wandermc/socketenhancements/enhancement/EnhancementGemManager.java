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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.wandermc.socketenhancements.config.Settings;
import net.wandermc.socketenhancements.gear.EnhancedItem;

/**
 * Manages the *use* (not creation) of Enhancement Gems.
 */
public class EnhancementGemManager implements Listener {
    private final JavaPlugin plugin;
    private final EnhancementManager manager;

    // Dummy enhancement gem for comparing in `.handlePlace()`
    private final ItemStack dummyGem;

    /**
     * Create an EnhancementGemManager for `plugin`.
     *
     * @param manager The current EnhancementManager
     */
    public EnhancementGemManager(JavaPlugin plugin, EnhancementManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        this.dummyGem = manager.createGemOfType(manager.get(""));

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Convert a bound Enhancement to an Enhancement Gem when interacting with a
     * grindstone.
     * 
     * Default 'interaction' is right-clicking while sneaking.
     *
     * @param event The event
     */
    @EventHandler
    public void handleInteract(PlayerInteractEvent event) {
        // TODO make interaction configurable
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock().getType() != Material.GRINDSTONE)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (!event.getPlayer().isSneaking())
            return;

        // Okay, we now know the player right-clicked a grindstone while sneaking.
        
        ItemStack item = event.getItem();
        if (item == null)
            return;
        EnhancedItem enhancedItem = new EnhancedItem(manager, item);

        Enhancement enhancement = enhancedItem.pop();
        if (enhancement instanceof EmptySocket)
            return;

        enhancedItem.update();

        event.getPlayer().getWorld().dropItemNaturally(
            event.getClickedBlock().getLocation(),
            manager.createGemOfType(enhancement)
        );
    }

    /**
     * Stop players from placing enhancement gems.
     *
     * @param event The event
     */
    @EventHandler
    public void handlePlace(BlockPlaceEvent event) {
        if (event.getItemInHand().isSimilar(dummyGem))
            event.setCancelled(true);
    }
}
