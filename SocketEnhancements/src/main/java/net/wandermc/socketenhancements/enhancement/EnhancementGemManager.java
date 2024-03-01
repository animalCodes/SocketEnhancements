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
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import net.wandermc.socketenhancements.gear.EnhancedItem;
import net.wandermc.socketenhancements.config.Settings;

/**
 * Manages the *use* (not creation) of Enhancement Gems.
 *
 * To be more specific, while EnhancementManager handles the creation of Enhancement Gems, 
 * (`.createGemOfType()`) this makes them actually usable. Allowing for them to be obtained 
 * and added to items, and stopping them from being placed.
 */
public class EnhancementGemManager implements Listener {
    private final JavaPlugin plugin;
    private final EnhancementManager manager;

    private final ItemStack dummyGem;

    /**
     * Create an EnhancementGemManager for `plugin`.
     *
     * @param plugin The plugin this manager is working for
     * @param manager The current EnhancementManager
     */
    public EnhancementGemManager(JavaPlugin plugin, EnhancementManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        this.dummyGem = manager.createGemOfType(manager.get(""));

        registerRecipe();

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates and registers recipe for applying Enhancement Gems to items.
     */
    private void registerRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(
                new NamespacedKey(Settings.NAMESPACE, "enhancement_gem_addition"), 
                new ItemStack(Material.STONE, 1));

        // If an item has a socket limit, it can be enhanced
        recipe.addIngredient(new RecipeChoice.MaterialChoice(
                Settings.SOCKET_LIMITS.keySet().stream().collect(Collectors.toList())));
        recipe.addIngredient(dummyGem.getType());

        Bukkit.addRecipe(recipe);
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
        if (!event.getPlayer().isSneaking())
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        ItemStack item = event.getItem();
        if (item == null)
            return;

        // Okay, we now know the player right-clicked a grindstone while sneaking 
        // and holding an item.
        
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
     * Add Enhancements to an item when combined with Enhancement Gems in a crafting table.
     *
     * @param event The event
     */
    @EventHandler
    public void handleCraft(PrepareItemCraftEvent event) {
        // Find gems and item to be enhanced in crafting "matrix"
        ArrayList<Enhancement> enhancements = new ArrayList<>();
        EnhancedItem itemToEnhance = null;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null) // Empty slots are represented by null
                continue;

            if (item.getType() == dummyGem.getType()) {
                Enhancement enhancement = new EnhancedItem(manager, item).pop();
                if (enhancement instanceof EmptySocket) {
                    // Normal end crystal in crafting table, if the recipe still matches 
                    // hide the stone block dummy result.
                    ItemStack result = event.getInventory().getResult();
                    if (result != null && result.getType() == Material.STONE)
                        event.getInventory().setResult(new ItemStack(Material.AIR));
                    // Prevent normal end crystal from being consumed if recipe is crafted.
                    return;
                } else {
                    enhancements.add(enhancement);
                }
            } else
                itemToEnhance = new EnhancedItem(manager, item.clone());
        }

        // Maybe we shouldn't add an empty list to a none-existent item, idk.
        if (enhancements.size() < 1 || itemToEnhance == null)
            return;

        // Try to add all enhancements
        // if a binding fails, don't allow the player to take the item.
        for (Enhancement enhancement : enhancements) {
            if (!itemToEnhance.bind(enhancement)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }

        event.getInventory().setResult(itemToEnhance.update());
    }

    /**
     * Stop players from placing enhancement gems.
     *
     * @param event The event
     */
    @EventHandler
    public void handlePlace(EntityPlaceEvent event) {
        ItemStack placedItem = event.getPlayer().getInventory().getItem(event.getHand());
        // If placed item is an end crystal with an enhancement, aka an Enhancement Gem
        if (placedItem.getType() == dummyGem.getType() &&
            !(new EnhancedItem(manager, placedItem).pop() instanceof EmptySocket))
            event.setCancelled(true);
    }
}
