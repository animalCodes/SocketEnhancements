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
package net.wandermc.socketenhancements.util.event;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A helper class to easily block items from being used in certain ways.
 *
 * Whether an event will be blocked is ultimately decided by the `itemChecker`,
 * this predicate will be given an item from the event. If the predicate returns
 * `true`, the event will be blocked.
 *
 * Any null ItemStacks given by the event will be converted into empty
 * ItemStacks.
 */
public class ItemEventBlocker implements Listener {
    private final JavaPlugin plugin;
    private final Predicate<ItemStack> itemChecker;
    private final EventPriority priority;

    /**
     * Create an ItemEventBlocker to block `actions` decided by `predicate`.
     *
     * Uses default event priority of LOWEST.
     *
     * @param plugin The JavaPlugin to run under.
     * @param itemChecker Determines whether a given item in an event should
     *                    cause that event to be cancelled.
     * @param actions The actions to block.
     */
    public ItemEventBlocker(JavaPlugin plugin, Predicate<ItemStack> itemChecker,
        BlockableAction ...actions) {
        this(plugin, itemChecker, EventPriority.LOWEST, actions);
    }

    /**
     * Create an ItemEventBlocker to block `actions` decided by `predicate`.
     *
     * @param plugin The JavaPlugin to run under.
     * @param itemChecker Determines whether a given item in an event should
     *                    cause that event to be cancelled.
     * @param priority The event priority of all event handlers.
     * @param actions The actions to block.
     */
    public ItemEventBlocker(JavaPlugin plugin, Predicate<ItemStack> itemChecker,
        EventPriority priority, BlockableAction ...actions) {
        this.plugin = plugin;
        this.itemChecker = itemChecker;
        this.priority = priority;

        registerHandlers(actions);
    }

    private void registerHandlers(BlockableAction ...actions) {
        for (BlockableAction action : actions) {
            try {
                Method handler;
                switch (action) {
                    case BLOCK_PLACE:
                        handler = this.getClass().getMethod("blockBlockPlace",
                            action.eventType());
                        break;
                    case FUEL_BREWING:
                        handler = this.getClass().getMethod("blockFuelBrewing",
                            action.eventType());
                        break;
                    case BREW_INGREDIENT:
                        handler = this.getClass().getMethod(
                            "blockBrewIngredient", action.eventType());
                        break;
                    case BURN:
                        handler = this.getClass().getMethod("blockBurn",
                            action.eventType());
                        break;
                    case COMBINE:
                        handler = this.getClass().getMethod("blockCombine",
                            action.eventType());
                        break;
                    case COOK:
                        handler = this.getClass().getMethod("blockCook",
                            action.eventType());
                        break;
                    case ENCHANT:
                        handler = this.getClass().getMethod("blockEnchant",
                            action.eventType());
                        break;
                    case ENTITY_PLACE:
                        handler = this.getClass().getMethod("blockEntityPlace",
                            action.eventType());
                        break;
                    case ENTITY_SPAWN:
                        handler = this.getClass().getMethod("blockEntitySpawn",
                            action.eventType());
                        break;
                    case GRIND:
                        handler = this.getClass().getMethod("blockGrind",
                            action.eventType());
                        break;
                    case SMELT:
                        handler = this.getClass().getMethod("blockSmelt",
                            action.eventType());
                        break;
                    case USE_IN_RECIPE:
                        handler = this.getClass().getMethod("blockUseInRecipe",
                            action.eventType());
                        break;
                    default:
                        // In case of unrecognised action, deliberately cause a
                        // NoSuchMethodException. (Also causes handler to be set
                        // in all branches, which makes javac happy.)
                        handler = this.getClass().getMethod("Foo");
                }
                registerHandler(handler, action.eventType());
            } catch (NoSuchMethodException exception) {
                plugin.getLogger().log(Level.SEVERE, this.getClass().getName()+
                    " Encountered an exception while setting up blockers.");
                plugin.getLogger().log(Level.SEVERE,
                    "Unable to locate method handler for "+action.eventType()
                    .getName()+". As such, those events will NOT be blocked.");
            }
        }
    }

    private void registerHandler(Method handler,
        Class<? extends Event> eventType) {
        plugin.getServer().getPluginManager().registerEvent(
            eventType,
            this,
            this.priority,
            EventExecutor.create(handler, eventType),
            plugin,
            true
        );
    }

    /**
     * Prevent a block from being placed if the placed item matches.
     */
    public void blockBlockPlace(BlockPlaceEvent event) {
        // blockBlockBlockBlockBlockBlockBlock
        if (itemChecker.test(event.getItemInHand()))
            event.setCancelled(true);
    }

    /**
     * Prevent matching item from being used as fuel in a brewing stand.
     */
    public void blockFuelBrewing(BrewingStandFuelEvent event) {
        if (itemChecker.test(event.getFuel()))
            event.setCancelled(true);
    }

    /**
     * Prevent matching item from being used as an ingredient in a brewing
     * stand.
     *
     * Item will appear to be applied at first, but will not be taken.
     */
    public void blockBrewIngredient(BrewEvent event) {
        if (itemChecker.test(nullEmpty(event.getContents().getIngredient())))
            event.setCancelled(true);
    }

    /**
     * Prevent matching items from being used as fuel.
     */
    public void blockBurn(FurnaceBurnEvent event) {
        if (itemChecker.test(nullEmpty(event.getFuel())))
            event.setCancelled(true);
    }

    /**
     * Prevent items from being combined in an anvil if either matches.
     */
    public void blockCombine(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getFirstItem();
        ItemStack second = event.getInventory().getSecondItem();
        // If we just checked whether one of the items matched, we'd end up
        // catching rename events as well.
        if ((itemChecker.test(nullEmpty(first)) && second != null)
            || (itemChecker.test(nullEmpty(second)) && first != null))
            event.setResult(null);
    }

    /**
     * Prevent matching items from being enchanted.
     */
    public void blockEnchant(PrepareItemEnchantEvent event) {
        if (itemChecker.test((event.getItem())))
            event.setCancelled(true);
    }

    /**
     * Prevent matching items from being cooked on campfires.
     *
     * Specifically, prevents the item from being placed on the campfire in the
     * first place.
     */
    public void blockCook(PlayerInteractEvent event) {
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK
            && (event.getClickedBlock().getType() == Material.CAMPFIRE
                || event.getClickedBlock().getType() == Material.SOUL_CAMPFIRE)
            && (event.hasItem()
                && itemChecker.test(nullEmpty(event.getItem()))))
            {event.setCancelled(true);}
    }

    /**
     * Prevent an entity from being placed if the placed item matches.
     */
    public void blockEntityPlace(EntityPlaceEvent event) {
        if (itemChecker.test(nullEmpty(
            event.getPlayer().getInventory().getItem(event.getHand()))))
            event.setCancelled(true);
    }

    /**
     * Prevent an entity from being spawned if the spawning item matches.
     */
    public void blockEntitySpawn(PlayerInteractEvent event) {
        if ((event.hasItem() && itemChecker.test(nullEmpty(event.getItem()))) &&
            (event.getAction() == Action.RIGHT_CLICK_AIR
            || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            event.setCancelled(true);
    }

    /**
     * Prevent matching items from being repaired / having enchantments removed
     * in a grindstone.
     */
    public void blockGrind(PrepareGrindstoneEvent event) {
        if (itemChecker.test(nullEmpty(event.getInventory().getUpperItem()))
            || itemChecker.test(nullEmpty(event.getInventory().getLowerItem())))
            event.setResult(null);
    }

    /**
     * Prevent matching items from being smelted in a furnace, smoker or blast
     * furnace.
     *
     * Item will appear to be smelted at first, but on completion no result
     * will appear and the item won't be taken.
     */
    public void blockSmelt(FurnaceSmeltEvent event) {
        if (itemChecker.test(nullEmpty(event.getSource())))
            event.setCancelled(true);
    }

    /**
     * Prevent an item from being crafted if one of the ingredients matches.
     */
    public void blockUseInRecipe(PrepareItemCraftEvent event) {
        for (ItemStack item : (event.getInventory().getMatrix())) {
            if (itemChecker.test(nullEmpty(item))) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    /**
     * If itemStack is null, return `ItemStack.empty()`. Otherwise itemStack.
     */
    private ItemStack nullEmpty(ItemStack itemStack) {
        return (itemStack == null) ? ItemStack.empty() : itemStack;
    }
}
