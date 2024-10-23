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

import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.executor.MethodHandleEventExecutor;

import net.wandermc.socketenhancements.events.AggregateEventListener;

/**
 * Manages storing, registering and activating enhancements.
 *
 * Only one EnhancementManager should be active at a time, as creating multiple
 * could lead to enhancements being registered multiple times.
 */
public class EnhancementManager {
    private final JavaPlugin plugin;

    private final HashMap<String, Enhancement> enhancementStore =
        new HashMap<String, Enhancement>();
    private final ArrayList<AggregateEventListener<? extends Event>> listeners =
        new ArrayList<>();

    private final EmptySocket emptySocket;

    /**
     * Create an EnhancementManager for `plugin`
     *
     * @param plugin The plugin this manager will run under.
     * @param emptySocket EmptySocket instance to use.
     */
    public EnhancementManager(JavaPlugin plugin, EmptySocket emptySocket) {
        this.plugin = plugin;
        this.emptySocket = emptySocket;
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
     * Registers `enhancement` with the appropriate AggregateEventListener.
     *
     * If no matching AggregateEventListener exists, one will be created.
     *
     * @param enhancement The enhancement to register
     */
    private <C extends Event> void registerActiveEnhancement(
        ActiveEnhancement<C> enhancement) {
        for (AggregateEventListener<?> activeListener : listeners) {
            // If there's already a listener with a matching eventType, use it
            if (activeListener.getEventType() == enhancement.getEventType()) {
                ((AggregateEventListener<C>) activeListener).add(enhancement);
                return;
            }
        }

        // No listener with a matching eventType was found, so create one
        listeners.add(new AggregateEventListener<C>(enhancement));
    }

    /**
     * Activates every currently-stored Enhancement.
     */
    public void activateEnhancements() {
        listeners.trimToSize();

        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        listeners.forEach(listener -> {
            pluginManager.registerEvent(
                    listener.getEventType(),
                    listener,
                    EventPriority.NORMAL,
                    new MethodHandleEventExecutor(listener.getEventType(),
                         listener.getHandler()),
                    plugin);
        });
    }

    /**
     * Stores and registers `enhancement`.
     *
     * Note that the enhancement will only become active after calling
     * `activateEnhancements()`
     * 
     * @param enhancement The enhancement to store
     */
    public void store(Enhancement enhancement) {
        if (enhancement instanceof ActiveEnhancement<?> activeEnhancement)
            registerActiveEnhancement(activeEnhancement);

        // TODO log warning if enhancement isn't a valid extension of
        // Enhancement

        enhancementStore.put(normaliseName(enhancement.getName()), enhancement);
    }

    /**
     * Retrieves the enhancement stored under `name`.
     *
     * If the enhancement doesn't exist, an EmptySocket will be returned
     * instead.
     *
     * @param name The name of the enhancement
     * @return The enhancement, or an EmptySocket if it doesn't exist.
     */
    public Enhancement get(String name) {
        return enhancementStore.getOrDefault(normaliseName(name), emptySocket);
    }

    /**
     * Get this manager's EmptySocket instance.
     *
     * @return An EmptySocket
     */
    public Enhancement getEmpty() {
        return emptySocket;
    }

    /**
     * Gets all currently stored enhancements.
     *
     * @return All current enhancements.
     */
    public Collection<Enhancement> getAll() {
        return enhancementStore.values();
    }
}
