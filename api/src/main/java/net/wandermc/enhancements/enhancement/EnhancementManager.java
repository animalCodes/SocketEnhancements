package net.wandermc.enhancements.enhancement;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.executor.MethodHandleEventExecutor;

import net.wandermc.enhancements.events.AggregateEventListener;
import net.wandermc.enhancements.events.EventType;

/**
 * Class for storing an instance of each enhancement etc.
 */
public class EnhancementManager {
    private final JavaPlugin plugin;

    private final HashMap<String, Enhancement> enhancementStore = new HashMap<String, Enhancement>();
    private final LinkedList<AggregateEventListener<? extends Event>> listeners = new LinkedList<>();

    private final EmptySocket emptySocket = new EmptySocket();

    public EnhancementManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Normalises `name` to ease storage and retrieval of enhancements.
     *
     * @param name Starting name
     * @return Normalised version of `name`
     */
    private String normaliseName(String name) {
        // TODO expand this
        return name.toLowerCase();
    }

    /**
     * Registers `enhancement` with the appropriate AggregateEventListener, creating
     * one if needed.
     *
     * @param enhancement The enhancement to register
     */
    private <C extends Event> void registerActiveEnhancement(ActiveEnhancement<C> enhancement) {
        // Get the event on which `enhancement` should be run
        // TODO check annotation exists
        Class<?> event = enhancement.getClass().getAnnotation(EventType.class).value();

        AggregateEventListener<C> listener = null;

        for (AggregateEventListener<?> activeListener : listeners) {
            // If there's already a listener with a matching eventType, use it
            if (activeListener.getEventType() == event) {
                ((AggregateEventListener<C>) activeListener).add(enhancement);
                break;
            }
        }

        // No listener with a matching eventType was found, so create one
        if (listener == null)
            listener = new AggregateEventListener<C>(enhancement);

        listeners.add(listener);
    }

    /**
     * Activates every currently-stored Enhancement, making them available for use.
     */
    public void activateEnhancements() {
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        listeners.forEach(listener -> {
            pluginManager.registerEvent(listener.getEventType(),
                    listener,
                    EventPriority.NORMAL,
                    new MethodHandleEventExecutor(listener.getEventType(), listener.getHandler()),
                    plugin);
        });
    }

    /**
     * Stores and registers `enhancement`.
     * Note that the enhancement will only become active after calling
     * `activateEnhancements()`
     * 
     * @param enhancement The enhancement to store
     */
    public void store(Enhancement enhancement) {
        if (enhancement instanceof ActiveEnhancement<?> activeEnhancement)
            registerActiveEnhancement(activeEnhancement);

        enhancementStore.put(normaliseName(enhancement.getName()), enhancement);
    }

    /**
     * Retrieves the enhancement stored under `name`.
     * If the enhancement doesn't exist, an EmptySocket will be returned
     * instead.
     *
     * @param name The name of the enhancement
     * @return The enhancement, or null if it doesn't exist.
     */
    public Enhancement get(String name) {
        return enhancementStore.getOrDefault(normaliseName(name), emptySocket);
    }
}
