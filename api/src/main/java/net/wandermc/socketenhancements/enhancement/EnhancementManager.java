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

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages storing, registering and activating enhancements.
 *
 * Only one EnhancementManager should be active at a time, as creating multiple
 * could lead to enhancements being registered multiple times.
 */
public class EnhancementManager {
    private final PluginManager pluginManager;
    private final JavaPlugin plugin;

    private final HashMap<String, Enhancement> enhancementStore =
        new HashMap<String, Enhancement>();

    private final EmptySocket emptySocket;

    /**
     * Create an EnhancementManager for `plugin`.
     *
     * @param plugin The plugin this manager will run under.
     * @param emptySocket EmptySocket instance to use.
     */
    public EnhancementManager(JavaPlugin plugin, EmptySocket emptySocket) {
        this.pluginManager = plugin.getServer().getPluginManager();
        this.plugin = plugin;
        this.emptySocket = emptySocket;
    }

    /**
     * Normalise Enhancement name `name`.
     *
     * @param name Name of Enhancement.
     * @return Normalised version of `name`
     */
    private String normalise(String name) {
        // TODO expand this
        return name.toLowerCase();
    }

    /**
     * Store and activate `enhancement`.
     *
     * If the enhancement is already registered, false will be returned and no
     * further action will be taken.
     *
     * @param enhancement The Enhancement to store.
     * @return Whether the enhancement was registered.
     * @throws IllegalArgumentException If `enhancement` does not implement
     *         `org.bukkit.event.Listener.
     */
    public boolean register(Enhancement enhancement) {
        if (enhancementStore.containsKey(normalise(enhancement.name())))
            return false;

        if (enhancement instanceof Listener listener) {
            pluginManager.registerEvents(listener, plugin);
        } else {
            throw new IllegalArgumentException("enhancement \"" +
                enhancement.name() +
                "\" does not implement org.bukkit.event.Listener.");
        }

        enhancementStore.put(normalise(enhancement.name()), enhancement);
        return true;
    }

    /**
     * Retrieve the enhancement stored under `name`.
     *
     * If the enhancement doesn't exist, an EmptySocket will be returned
     * instead.
     *
     * @param name The name of the Enhancement.
     * @return The Enhancement.
     */
    public Enhancement get(String name) {
        return enhancementStore.getOrDefault(normalise(name), emptySocket);
    }

    /**
     * Get this manager's EmptySocket instance.
     *
     * @return An EmptySocket.
     */
    public Enhancement empty() {
        return emptySocket;
    }

    /**
     * Get all currently stored enhancements.
     *
     * @return All stored enhancements.
     */
    public Collection<Enhancement> getAll() {
        return enhancementStore.values();
    }
}
