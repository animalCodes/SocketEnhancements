package net.wandermc.socketenhancements.events;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.function.Predicate;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.executor.MethodHandleEventExecutor;

/**
 * A helper class to easily block items from being used in certain ways.
 *
 * Whether an event will be blocked is ultimately decided by the `itemChecker`,
 * this predicate will be given an item from the event. If the predicate returns
 * `true`, the event will be blocked.
 */
public class ItemEventBlocker implements Listener {
    private final JavaPlugin plugin;
    private final Predicate<ItemStack> itemChecker;

    /**
     * Create an ItemEventBlocker to block `actions` decided by `predicate`.
     *
     * @param plugin The JavaPlugin to run under.
     * @param itemChecker Determines whether a given item in an event should
     *                    cause that event to be cancelled.
     * @param actions The actions to block.
     */
    public ItemEventBlocker(JavaPlugin plugin, Predicate<ItemStack> itemChecker,
        BlockableAction ...actions) {
        this.plugin = plugin;
        this.itemChecker = itemChecker;

        registerHandlers(actions);
    }

    /**
     * An "action" that can be blocked by an ItemEventBlocker.
     */
    public static enum BlockableAction {
        /**
         * An entity being placed.
         *
         * Used for end crystals, item frames and the like.
         */
        ENTITY_PLACE (EntityPlaceEvent.class),
        /**
         * A block being placed.
         */
        BLOCK_PLACE (BlockPlaceEvent.class);

        private final Class<? extends Event> eventType;

        BlockableAction(Class<? extends Event> eventType) {
            this.eventType = eventType;
        }

        public Class<? extends Event> eventType() {
            return this.eventType;
        }
    }

    private void registerHandlers(BlockableAction ...actions) {
        for (BlockableAction action : actions) {
            // Try to find the handler method for this action
            try {
                switch (action) {
                    case ENTITY_PLACE:
                        registerHandler(this.getClass()
                            .getMethod("blockEntityPlace", action.eventType()),
                            action.eventType());
                        break;
                    case BLOCK_PLACE:
                        registerHandler(this.getClass()
                            .getMethod("blockBlockPlace", action.eventType()),
                            action.eventType());
                        break;
                }
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
            EventPriority.LOWEST, // TODO let priority be set in constructor
            new MethodHandleEventExecutor(eventType, handler),
            plugin
        );
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
     * Prevents a block from being placed if the placed item matches.
     */
    public void blockBlockPlace(BlockPlaceEvent event) {
        if (itemChecker.test(event.getItemInHand()))
            event.setCancelled(true);
    }
}
