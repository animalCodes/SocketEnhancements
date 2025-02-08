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

import org.bukkit.Bukkit;
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
        this.plugin = plugin;
        this.emptySocket = emptySocket;
    }

    /**
     * Normalise `name`.
     *
     * @param name Name of Enhancement.
     * @return Normalised version of `name`
     */
    private String normaliseName(String name) {
        // TODO expand this
        return name.toLowerCase();
    }

    /**
     * Activate every currently-stored Enhancement.
     */
    public void activateEnhancements() {
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        for (Enhancement e : enhancementStore.values()) {
            if (e instanceof Listener listener)
                pluginManager.registerEvents(listener, plugin);
            else
                plugin.getLogger().warning("Couldn't activate enhancement " + e
                    + ". Because it is not an instance of Listener.");
        }
    }

    /**
     * Register `enhancement`.
     *
     * Note that the Enhancement will only become active after calling
     * `activateEnhancements()`
     * 
     * @param enhancement The Enhancement to store.
     */
    public void store(Enhancement enhancement) {
        enhancementStore.put(normaliseName(enhancement.name()), enhancement);
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
        return enhancementStore.getOrDefault(normaliseName(name), emptySocket);
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
