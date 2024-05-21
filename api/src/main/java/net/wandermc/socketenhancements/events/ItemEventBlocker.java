package net.wandermc.socketenhancements.events;

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
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.executor.MethodHandleEventExecutor;

/**
 * A helper class to easily block items from being used in certain ways.
 *
 * Whether an event will be blocked is ultimately decided by the `itemChecker`,
 * this predicate will be given an item from the event. If the predicate returns
 * `true`, the event will be blocked.
 *
 * Please note that as the ItemStack is passed directly from the event to the
 * predicate, it may be null.
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
            // Try to find the handler method for this action
            try {
                Method handler;
                switch (action) {
                    case BLOCK_PLACE:
                        handler = this.getClass().getMethod("blockBlockPlace",
                            action.eventType());
                        break;
                    case BREW_FUEL:
                        handler = this.getClass().getMethod("blockBrewFuel",
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
                // No such method - this shouldn't happen!
                plugin.getLogger().log(Level.SEVERE, this.getClass().getName()+
                    " Encountered an exception while setting up blockers.");
                plugin.getLogger().log(Level.SEVERE,
                    "Unable to locate method handler for "+action.eventType()
                    .getName()+". As such, those events will NOT be blocked.");
            }
        }
    }

    private void registerHandler(Method handler, Class<? extends Event> eventType) {
        plugin.getServer().getPluginManager().registerEvent(
            eventType,
            this,
            this.priority,
            new MethodHandleEventExecutor(eventType, handler),
            plugin,
            true
        );
    }

    /**
     * Prevents a block from being placed if the placed item matches.
     */
    public void blockBlockPlace(BlockPlaceEvent event) {
        // blockBlockBlockBlockBlockBlockBlock
        if (itemChecker.test(event.getItemInHand()))
            event.setCancelled(true);
    }

    /**
     * Prevents a matching item from being used as fuel in a brewing stand.
     */
    public void blockBrewFuel(BrewingStandFuelEvent event) {
        if (itemChecker.test(event.getFuel()))
            event.setCancelled(true);
    }

    /**
     * Prevents a matching item from being used as an ingredient in a brewing
     * stand.
     *
     * Item will appear to be applied at first, but will not be taken.
     */
    public void blockBrewIngredient(BrewEvent event) {
        if (itemChecker.test(event.getContents().getIngredient()))
            event.setCancelled(true);
    }

    /**
     * Prevents matching items from being used as fuel.
     */
    public void blockBurn(FurnaceBurnEvent event) {
        if (itemChecker.test(event.getFuel()))
            event.setCancelled(true);
    }
    /**
     * Prevents items from being combined in an anvil if either matches.
     */
    public void blockCombine(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getFirstItem();
        ItemStack second = event.getInventory().getSecondItem();
        // If we just checked whether one of the items matched, we'd end up
        // catching rename events as well.
        if ((itemChecker.test(first) && second != null)
            || (itemChecker.test(second) && first != null))
            event.setResult(null);
    }

    /**
     * Prevents matching items from being enchanted
     */
    public void blockEnchant(PrepareItemEnchantEvent event) {
        if (itemChecker.test(event.getItem()))
            event.setCancelled(true);
    }

    /**
     * Prevents matching items from being cooked on campfires.
     *
     * Specifically, prevents the item from being placed on the campfire in the
     * first place.
     */
    public void blockCook(PlayerInteractEvent event) {
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK &&
            (event.getClickedBlock().getType() == Material.CAMPFIRE
            || event.getClickedBlock().getType() == Material.SOUL_CAMPFIRE)) {
                if (event.hasItem() && itemChecker.test(event.getItem()))
                    event.setCancelled(true);
            }
    }

    /**
     * Prevents an entity from being placed if the placed item matches.
     */
    public void blockEntityPlace(EntityPlaceEvent event) {
        if (itemChecker.test(
            event.getPlayer().getInventory().getItem(event.getHand())))
            event.setCancelled(true);
    }

    /**
     * Prevents matching items from being repaired / having enchantments removed
     * in a grindstone.
     */
    public void blockGrind(PrepareGrindstoneEvent event) {
        if (itemChecker.test(event.getInventory().getUpperItem())
            || itemChecker.test(event.getInventory().getLowerItem()))
            event.setResult(null);
    }

    /**
     * Prevents matching items from being smelted in a furnace, smoker or blast
     * furnace.
     *
     * Item will appear to be smelted at first, but on completion no result
     * will appear and the item won't be taken.
     */
    public void blockSmelt(FurnaceSmeltEvent event) {
        if (itemChecker.test(event.getSource()))
            event.setCancelled(true);
    }

    /**
     * Prevents an item from being crafted if one of the ingredients matches.
     */
    public void blockUseInRecipe(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (itemChecker.test(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
