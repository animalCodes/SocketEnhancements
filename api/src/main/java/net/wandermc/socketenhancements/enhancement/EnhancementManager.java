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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.executor.MethodHandleEventExecutor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.config.Settings;
import net.wandermc.socketenhancements.events.AggregateEventListener;
import net.wandermc.socketenhancements.gear.EnhancedItem;

/**
 * Manages storing, registering and activating enhancements.
 * Only one EnhancementManager should ever be constructed, as creating multiple
 * could lead to enhancements being registered multiple times.
 */
public class EnhancementManager {
    private static final TextComponent ENHANCEMENT_GEM_NAME = Component.text("Enhancement Gem", 
            Style.style(TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)));
    public static final Material ENHANCEMENT_GEM_TYPE = Material.END_CRYSTAL;

    private final JavaPlugin plugin;

    private final HashMap<String, Enhancement> enhancementStore = new HashMap<String, Enhancement>();
    private final LinkedList<AggregateEventListener<? extends Event>> listeners = new LinkedList<>();

    private final EmptySocket emptySocket = new EmptySocket();

    /**
     * Create an EnhancementManager for `plugin`
     *
     * @param plugin The plugin this manager will run under.
     */
    public EnhancementManager(JavaPlugin plugin) {
        this.plugin = plugin;

        Settings.SOCKET_LIMITS.put(ENHANCEMENT_GEM_TYPE, 1);
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
        AggregateEventListener<C> listener = null;

        for (AggregateEventListener<?> activeListener : listeners) {
            // If there's already a listener with a matching eventType, use it
            if (activeListener.getEventType() == enhancement.getEventType()) {
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
            // Yuck
            pluginManager.registerEvent(
                    listener.getEventType(),
                    listener,
                    EventPriority.NORMAL,
                    new MethodHandleEventExecutor(listener.getEventType(), listener.getHandler()),
                    plugin);
        });
    }

    /**
     * Create an enhancement gem of type `enhancement`.
     *
     * An "Enhancement Gem" is an ItemStack of type `Settings.enhancementGemType`
     * and a single socket. The enhancement in that socket is the "type" of the
     * Enhancement Gem.
     *
     * @param enhancement The Enhancement the gem represents.
     * @return An Enhancement Gem.
     */
    public ItemStack createGemOfType(Enhancement enhancement) {
        ItemStack item = new ItemStack(ENHANCEMENT_GEM_TYPE);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(ENHANCEMENT_GEM_NAME);
        item.setItemMeta(meta);

        EnhancedItem enhancedItem = new EnhancedItem(this, item);
        enhancedItem.addSockets(1);
        enhancedItem.bind(enhancement);

        return enhancedItem.update();
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

    /**
     * Gets all currently stored enhancements.
     *
     * @return All current enhancements.
     */
    public Collection<Enhancement> getAll() {
        enhancementStore.values().removeIf(e -> e instanceof EmptySocket);
        return enhancementStore.values();
    }
}
