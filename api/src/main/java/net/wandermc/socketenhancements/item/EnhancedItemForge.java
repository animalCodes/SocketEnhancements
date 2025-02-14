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
package net.wandermc.socketenhancements.item;

import java.util.ArrayList;
import java.util.Set;
import java.util.EnumMap;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;

import net.wandermc.socketenhancements.enhancement.Enhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;

/**
 * Class for the creation of EnhancedItem instances.
 *
 * Where EnhancementManager is responsible for managing Enhancements,
 * EnhancedItem handles storing those Enhancements on items.
 */
public class EnhancedItemForge {
    private final EnhancementManager manager;
    private final NamespacedKey socketsKey;
    private final EnumMap<Material, Integer> socketLimits;
    private final int defaultSocketLimit;

    /**
     * Create an EnhancedItemForge for `plugin`.
     *
     * @param plugin The plugin this 'Forge is working for.
     * @param manager `plugin`'s EnhancementManager.
     * @param socketLimits Limits for how many sockets can be applied to certain
     *                     items.
     * @param defaultSocketLimit Socket limit for any other item.
     */
    public EnhancedItemForge(JavaPlugin plugin, EnhancementManager manager,
        EnumMap<Material, Integer> socketLimits, int defaultSocketLimit) {
        this.manager = manager;
        this.socketsKey = new NamespacedKey(plugin, "sockets");

        this.socketLimits = socketLimits;
        this.defaultSocketLimit = defaultSocketLimit;
    }

    /**
     * Create an EnhancedItem around `item`.
     *
     * Note that `.update()` will need to be called for any subsequent changes
     * to be applied.
     *
     * @param item The item to work on.
     * @return An EnhancedItem to manage Enhancements on `item`.
     * @throws IllegalArgumentException if `item` has null ItemMeta.
     */
    public EnhancedItem create(ItemStack item) {
        if (item.getItemMeta() == null)
            throw new IllegalArgumentException("Item has null ItemMeta, "+
                "possibly because it's AIR.");
        return new EnhancedItem(item);
    }

    /**
     * All Material's with a socket limit defined.
     *
     * @return All enhanceable materials.
     */
    public Set<Material> enhanceableMaterials() {
        return socketLimits.keySet();
    }

    /**
     * Socket limit set for `mat`, or -1 if not defined.
     *
     * @return Socket limit for `mat`.
     */
    public int socketLimit(Material mat) {
        return socketLimits.get(mat);
    }

    /**
     * How many sockets are currently on `item`.
     *
     * Shortcut for create(item).sockets();
     *
     * @return The number of sockets.
     */
    public int sockets(ItemStack item) {
        return create(item).sockets();
    }

    /**
     * Whether `item` has `enhancement` currently bound to it.
     *
     * Shortcut for create(item).has(enhancement)
     *
     * @param enhancement The Enhancement to look for.
     * @return Whether it's bound.
     */
    public boolean has(ItemStack item, Enhancement enhancement) {
        return create(item).has(enhancement.name());
    }

    /**
     * Whether `item` has `enhancementName` currently bound to it.
     *
     * Shortcut for create(item).has(enhancementName)
     *
     * @param enhancement The name of the Enhancement to look for.
     * @return Whether it's bound.
     */
    public boolean has(ItemStack item, String enhancementName) {
        return create(item).has(enhancementName);
    }

    /**
     * A wrapper class for reading, updating and removing sockets and
     * enhancements from ItemStacks.
     */
    public class EnhancedItem {
        private ItemStack item;
        private ItemMeta itemMeta;
        private ArrayList<String> socketList;

        /**
         * Create an EnhancedItem around `item`.
         * 
         * @param item The item to work on.
         */
        private EnhancedItem(ItemStack item) {
            this.item = item;
            this.itemMeta = item.getItemMeta();

            // Ensure the item has a socket list before doing anything with it
            PersistentDataContainer dataContainer = itemMeta
                .getPersistentDataContainer();
            if (!dataContainer.has(socketsKey))
                dataContainer.set(socketsKey, PersistentDataType.LIST.strings(),
                     new ArrayList<String>());

            // A PersistentDataType.LIST is immutable, but we need to modify it.
            // So create a new ArrayList based on the ArrayList we might have
            // literally just stored on the item.
            this.socketList = new ArrayList<String>(dataContainer
                .get(socketsKey, PersistentDataType.LIST.strings()));
        }

        /**
         * Update the item's lore to match socketList.
         */
        private void updateLore() {
            ArrayList<Component> lore = new ArrayList<Component>(
                socketList.size());

            socketList.forEach(socketId -> lore.add(manager.get(socketId)
                .socketMessage()));

            // Currently, SocketEnhancements greedily resets the entire lore
            // field, meaning any none-SE lore will be deleted. Ah well.
            itemMeta.lore(lore);
        }

        /**
         * The ItemStack this instance is holding.
         *
         * @return The ItemStack.
         */
        public ItemStack itemStack() {
            return item;
        }

        /**
         * How many sockets are currently on the item.
         *
         * @return The number of sockets.
         */
        public int sockets() {
            return socketList.size();
        }

        /**
         * Whether there is at least one empty socket on the item.
         *
         * @return Whether there is an empty socket.
         */
        public boolean hasEmptySocket() {
            return has(manager.empty());
        }

        /**
         * The maximum number of sockets that the item can have.
         *
         * @return The maximum allowed sockets.
         */
        public int socketLimit() {
            return socketLimits.getOrDefault(item.getType(),
                defaultSocketLimit);
        }

        /**
         * Add the specified number of sockets to the item.
         *
         * Note that this will **not** check if adding the given number of
         * sockets would push the item over it's socket limit.
         * 
         * @param sockets The number of sockets to add.
         */
        public void addSockets(int sockets) {
            for (int i = 0; i < sockets; i++) {
                // Can't use `bind()` as unlike normal enhancements empty
                // sockets can very much be 'bound' multiple times.
                socketList.add(manager.empty().name());
            }
        }

        /**
         * Whether the item has `enhancement` currently bound to it.
         *
         * @param enhancement The Enhancement to look for.
         * @return Whether it's bound.
         */
        public boolean has(Enhancement enhancement) {
            return has(enhancement.name());
        }

        /**
         * Whether the item has `enhancementName` currently bound to it.
         *
         * @param enhancement The name of the Enhancement to look for.
         * @return Whether it's bound.
         */
        public boolean has(String enhancementName) {
            return socketList.contains(enhancementName);
        }

        /**
         * Remove `enhancement` from the item.
         *
         * @param enhancement The enhancement to remove.
         * @return Whether the enhancement was present.
         */
        public boolean remove(Enhancement enhancement) {
            return remove(enhancement.name());
        }

        /**
         * Remove `enhancementName` from the item.
         *
         * @param enhancement The name of the enhancement to remove.
         * @return Whether the enhancement was present.
         */
        public boolean remove(String enhancementName) {
            int index = socketList.indexOf(enhancementName);
            if (index < 0)
                return false;

            socketList.set(index, manager.empty().name());
            return true;
        }

        /**
         * Get the Enhancement at index `i`.
         *
         * @return The Enhancement.
         * @throws IndexOutOfBoundsException if `i < 0 || i > sockets()`.
         */
        public Enhancement get(int i) {
            return manager.get(socketList.get(i));
        }

        /**
         * Attempt to bind `enhancementName` to the item.
         *
         * The following checks must pass for the binding to be successful:
         * - This item is valid for the enhancement.
         * - Item doesn't already have `enhancement`.
         * - An empty socket is available.
         *
         * @param enhancement The name of the enhancement to bind.
         * @return Whether the binding was successful.
         */
        public boolean bind(String enhancementName) {
            return bind(manager.get(enhancementName));
        }

        /**
         * Attempt to bind `enhancement` to the item.
         *
         * The following checks must pass for the binding to be successful:
         * - This item is valid for the enhancement.
         * - Item doesn't already have `enhancement`.
         * - An empty socket is available.
         *
         * @param enhancement The enhancement to bind
         * @return Whether the binding was successful.
         */
        public boolean bind(Enhancement enhancement) {
            if (!enhancement.isValidItem(this))
                return false;

            // Can't bind an enhancement more than once.
            if (has(enhancement))
                return false;

            return checklessBind(enhancement);
        }

        /**
         * Bind `enhancementName` to item without checks.
         *
         * Bypassed checks are whether the item is valid for this enhancement
         * and whether the item already has the enhancement.
         *
         * The item must still have an empty socket.
         *
         * @param enhancementName the name of the Enhancement to bind.
         * @return Whether binding was successful.
         */
        public boolean checklessBind(String enhancementName) {
            return checklessBind(manager.get(enhancementName));
        }

        /**
         * Bind `enhancement` to item without checks.
         *
         * Bypassed checks are whether the item is valid for this enhancement
         * and whether the item already has the enhancement.
         *
         * The item must still have an empty socket.
         *
         * @param enhancement The Enhancement to bind.
         * @return Whether binding was successful.
         */
        public boolean checklessBind(Enhancement enhancement) {
            int index = socketList.indexOf(manager.empty().name());
            if (index < 0)
                return false;
            else
                socketList.set(index, enhancement.name());
            return true;
        }

        /**
         * Apply all enhancement/socket changes to the ItemStack.
         * 
         * @return The ItemStack.
         */
        public ItemStack update() {
            itemMeta.getPersistentDataContainer().set(socketsKey,
                PersistentDataType.LIST.strings(), socketList);
            updateLore();
            item.setItemMeta(itemMeta);
            return item;
        }
    }
}
