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
package net.wandermc.socketenhancements.binding;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.config.Settings;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.gear.EnhancedItem;

/**
 * Manages the crafting and usage of orbs of binding.
 * Note that the only requirement to enable orbs of binding is to construct one of these.
 */
public class OrbOfBindingManager implements Listener {
    private final JavaPlugin plugin;
    private final EnhancementManager manager;

    private final ItemStack orbOfBinding;
    
    /**
     * Create an OrbOfBindingManager for `plugin`.
     *
     * @param plugin The plugin this manager is working for.
     * @param manager The current EnhancementManager.
     */
    public OrbOfBindingManager(JavaPlugin plugin, EnhancementManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        this.orbOfBinding = createOrbOfBinding();

        registerRecipes();

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Create an Orb of Binding.
     *
     * @return An Orb of Binding ItemStack.
     */
    private ItemStack createOrbOfBinding() {
        ItemStack orb = new ItemStack(Material.CONDUIT);
        ItemMeta meta = orb.getItemMeta();
        meta.displayName(Component.text("Orb of Binding", 
            Style.style(TextDecoration.ITALIC.withState(TextDecoration.State.FALSE))));
        orb.setItemMeta(meta);
        return orb;
    }

    /**
     * Creates and registers the recipes for crafting and applying orbs of binding.
     */
    private void registerRecipes() {
        // Recipe for crafting an actual orb of binding.
        ShapelessRecipe orbOfBindingRecipe = new ShapelessRecipe(
                new NamespacedKey(Settings.NAMESPACE, "orb_of_binding_craft"), orbOfBinding);

        // Players must travel great distances to obtain an orb of binding ..
        // TODO make recipe configurable
        orbOfBindingRecipe.addIngredient(Material.END_CRYSTAL);
        orbOfBindingRecipe.addIngredient(Material.PRISMARINE_SHARD);
        orbOfBindingRecipe.addIngredient(Material.CHORUS_FRUIT);

        Bukkit.addRecipe(orbOfBindingRecipe);


        // Recipe for adding an orb of binding to an item.
        ShapelessRecipe upgradeRecipe = new ShapelessRecipe(
                // Paper won't let me set the result to AIR, so in the (hopefully impossible) case where the recipe 
                // matches and the handler doesn't pick up on it, there will be a random stone block result..
                new NamespacedKey(Settings.NAMESPACE, "orb_of_binding_upgrade"), 
                new ItemStack(Material.STONE, 1));

        // Any item with a socket limit
        upgradeRecipe.addIngredient(new RecipeChoice.MaterialChoice(
                // Why is it so complicated to convert a Set to a List!?
                Settings.SOCKET_LIMITS.keySet().stream().collect(Collectors.toList())));
        upgradeRecipe.addIngredient(orbOfBinding);

        Bukkit.addRecipe(upgradeRecipe);
    }

    /**
     * Add sockets to an item when combined with one or more orbs of binding in a crafting table.
     *
     * @param event The event
     */
    @EventHandler
    public void handleCraft(PrepareItemCraftEvent event) {
        // Try to find orbs of binding and item being bound in crafting table
        int orbs = 0;
        EnhancedItem itemToUpgrade = null;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null) // Empty slots are represented by null
                continue;

            if (item.isSimilar(orbOfBinding))
                orbs++;
            else
                itemToUpgrade = new EnhancedItem(manager, item.clone());
        }
      
        // We only care about the event if the crafting matrix contains at least one orb 
        // of binding and another item.
        if (orbs < 1 || itemToUpgrade == null)
            return;

        if (itemToUpgrade.getSocketLimit() >= itemToUpgrade.getSockets() + orbs) {
            // Item exists and can have `orbs` sockets added, do so.
            itemToUpgrade.addSockets(orbs);
            event.getInventory().setResult(itemToUpgrade.update());
        } else {
            // In case item can't have the orbs added, hide dummy result item.
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    /**
     * Stop players from placing orbs of binding.
     *
     * @param event The event
     */
    @EventHandler
    public void handlePlace(BlockPlaceEvent event) {
        if (event.getItemInHand().isSimilar(orbOfBinding))
            event.setCancelled(true);
    }
}
