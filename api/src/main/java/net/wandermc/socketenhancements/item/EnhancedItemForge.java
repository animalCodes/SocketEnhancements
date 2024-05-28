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

import net.wandermc.socketenhancements.enhancement.EmptySocket;
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
     * @param socketLimits Limits for how many sockets can be applied to certain items.
     * @param defaultSocketLimit Socket limit for any item not in `socketLimits`.
     */
    public EnhancedItemForge(JavaPlugin plugin, EnhancementManager manager,
        EnumMap<Material, Integer> socketLimits, int defaultSocketLimit) {
        this.manager = manager;
        this.socketsKey = new NamespacedKey(plugin, "sockets");

        this.socketLimits = socketLimits;
        this.defaultSocketLimit = defaultSocketLimit;
    }

    /**
     * Create an EnhancedItemForge for `plugin`.
     *
     * @param plugin The plugin this 'Forge is working for.
     * @param manager `plugin`'s EnhancementManager.
     * @param socketLimits Limits for how many sockets can be applied to certain
                           items, with the default socket limit stored under
                           Material.AIR.
     */
    public EnhancedItemForge(JavaPlugin plugin, EnhancementManager manager,
        EnumMap<Material, Integer> socketLimits) {
        this(plugin, manager, socketLimits, socketLimits.getOrDefault(Material.AIR, 0));
    }

    /**
     * Create an EnhancedItem around `item`.
     * Note that `.update()` will need to be called for any subsequent changes
     * to be applied.
     * 
     * @param item The item to work on.
     * @return An EnhancedItem to manage Enhancements on `item`.
     */
    public EnhancedItem create(ItemStack item) {
        return new EnhancedItem(item);
    }

    /**
     * Get a set of all Material's with a socket limit defined.
     * 
     * @return All enhanceable materials.
     */
    public Set<Material> getEnhanceableMaterials() {
        return socketLimits.keySet();
    }

    /**
     * A wrapper class for reading, updating and removing sockets and enhancements
     * from ItemStacks.
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
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (!dataContainer.has(socketsKey))
                dataContainer.set(socketsKey, PersistentDataType.LIST.strings(), new ArrayList<String>());
            // A PersistentDataType.LIST is immutable, but we need to modify it.
            // So create a new ArrayList based on the ArrayList we might have literally just
            // stored on the item.
            this.socketList = new ArrayList<String>(dataContainer.get(socketsKey, PersistentDataType.LIST.strings()));
        }

        /**
         * Updates the item's lore to match socketList.
         */
        private void updateLore() {
            ArrayList<Component> lore = new ArrayList<Component>(socketList.size());

            // socketList is the internal list used to identify how many sockets the item
            // has and, if they are filled, what with. The lore is basically just a
            // user-friendly version of that information.
            socketList.forEach(socketId -> lore.add(manager.get(socketId).getSocketMessage()));

            // Currently, SocketEnhancements greedily resets the entire lore field, meaning
            // any none-SE lore will be deleted. Ah well.
            itemMeta.lore(lore);
        }

        /**
         * Gets how many sockets are currently on the item.
         *
         * @return The number of sockets
         */
        public int getSockets() {
            return socketList.size();
        }

        /**
         * Determines whether there is at least one empty socket on the item.
         *
         * @return Whether there is an empty socket.
         */
        public boolean hasEmptySocket() {
            return hasEnhancement(manager.getEmpty());
        }

        /**
         * Gets the maximum number of sockets that the item can have.
         *
         * @return The maximum
         */
        public int getSocketLimit() {
            return socketLimits.getOrDefault(item.getType(), defaultSocketLimit);
        }

        /**
         * Adds the specified number of sockets to the item.
         * Note that this will **not** check if adding the given number of sockets would
         * push the item over it's socket limit.
         * 
         * @param sockets The number of sockets to add
         */
        public void addSockets(int sockets) {
            for (int i = 0; i < sockets; i++) {
                // Can't use `bind()` as unlike normal enhancements empty sockets can very much
                // be 'bound' multiple times.
                socketList.add(manager.getEmpty().getName());
            }
        }

        /**
         * Returns whether the item has `enhancement` currently bound to it.
         *
         * @param enhancement The enhancement to look for
         * @return Whether it's bound.
         */
        public boolean hasEnhancement(Enhancement enhancement) {
            return hasEnhancement(enhancement.getName());
        }

        /**
         * Returns whether the item has `enhancementName` currently bound to it.
         *
         * @param enhancement The name of the enhancement to look for.
         * @return Whether it's bound.
         */
        public boolean hasEnhancement(String enhancementName) {
            return socketList.contains(enhancementName);
        }

        /**
         * Removes `enhancement` from the item.
         *
         * @param enhancement The enhancement to remove
         * @return Whether the enhancement was present.
         */
        public boolean removeEnhancement(Enhancement enhancement) {
            return removeEnhancement(enhancement.getName());
        }

        /**
         * Removes `enhancementName` from the item.
         *
         * @param enhancement The name of the enhancement to remove.
         * @return Whether the enhancement was present.
         */
        public boolean removeEnhancement(String enhancementName) {
            int index = socketList.indexOf(enhancementName);
            if (index < 0)
                return false;

            socketList.set(index, manager.getEmpty().getName());
            return true;
        }

        /**
         * Removes the last Enhancement from the item.
         *
         * @return The last Enhancement, or an EmptySocket if none are bound.
         */
        public Enhancement pop() {
            Enhancement enhancement = null;
            for (int i = 0; i < socketList.size(); i++) {
                if (manager.get(socketList.get(i)) instanceof EmptySocket)
                    break;

                enhancement = manager.get(socketList.get(i));
            }

            if (enhancement != null) {
                removeEnhancement(enhancement);
                return enhancement;
            } else 
                return manager.getEmpty();
        }

        /**
         * Attempts to bind `enhancementName` to the item.
         *
         * @param enhancement The name of the enhancement to bind.
         * @return Whether the binding was successful.
         */
        public boolean bind(String enhancementName) {
            return bind(manager.get(enhancementName));
        }

        /**
         * Attempts to bind `enhancement` to the item.
         *
         * @param enhancement The enhancement to bind
         * @return Whether the binding was successful.
         */
        public boolean bind(Enhancement enhancement) {
            if (!enhancement.isValidItem(this))
                return false;

            // Can't bind an enhancement more than once.
            if (hasEnhancement(enhancement))
                return false;

            int index = socketList.indexOf(manager.getEmpty().getName());
            if (index < 0)
                // No empty sockets
                return false;
            else
                // Yes empty sockets, fill it
                socketList.set(index, enhancement.getName());

            return true;
        }

        /**
         * Applies all enhancement/socket changes to the ItemStack.
         * 
         * @return The ItemStack.
         */
        public ItemStack update() {
            itemMeta.getPersistentDataContainer().set(socketsKey, PersistentDataType.LIST.strings(), socketList);
            updateLore();
            item.setItemMeta(itemMeta);
            return item;
        }
    }
}
