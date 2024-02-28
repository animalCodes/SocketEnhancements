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
package net.wandermc.socketenhancements.gear;

import java.util.ArrayList;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;

import net.wandermc.socketenhancements.config.Settings;
import net.wandermc.socketenhancements.enhancement.Enhancement;
import net.wandermc.socketenhancements.enhancement.EmptySocket;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;

/**
 * A wrapper class for reading, updating and removing sockets and enhancements
 * from ItemStacks.
 */
public class EnhancedItem {
    private static NamespacedKey socketsKey = new NamespacedKey(Settings.NAMESPACE, "sockets");

    private EnhancementManager enhancementManager;

    private ItemStack item;
    private ItemMeta itemMeta;
    private ArrayList<String> socketList;

    /**
     * Create an EnhancedItem around `item`.
     * Note that `.update()` will need to be called for any subsequent changes to be
     * applied.
     * 
     * @param manager The current EnhancementManager
     * @param item    The item to work on.
     */
    public EnhancedItem(EnhancementManager manager, ItemStack item) {
        this.enhancementManager = manager;

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
        socketList.forEach(socketId -> lore.add(enhancementManager.get(socketId).getSocketMessage()));

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
        return hasEnhancement(enhancementManager.get(""));
    }

    /**
     * Gets the maximum number of sockets that the item can have.
     *
     * @return The maximum
     */
    public int getSocketLimit() {
        return Settings.SOCKET_LIMITS.getOrDefault(item.getType(), Settings.DEFAULT_SOCKET_LIMIT);
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
            socketList.add(enhancementManager.get("").getName());
        }
    }

    /**
     * Returns whether the item has `enhancement` currently bound to it.
     *
     * @param enhancement The enhancement to look for
     * @return Whether it's bound.
     */
    public boolean hasEnhancement(Enhancement enhancement) {
        return socketList.contains(enhancement.getName());
    }

    /**
     * Removes `enhancement` from the item.
     *
     * @param enhancement The enhancement to remove
     * @return Whether the enhancement was present.
     */
    public boolean removeEnhancement(Enhancement enhancement) {
        int index = socketList.indexOf(enhancement.getName());
        if (index < 0)
            return false;

        socketList.set(index, enhancementManager.get("").getName());
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
            if (enhancementManager.get(socketList.get(i)) instanceof EmptySocket)
                break;

            enhancement = enhancementManager.get(socketList.get(i));
        }

        if (enhancement != null)
            removeEnhancement(enhancement);

        return enhancement;
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

        int index = socketList.indexOf(enhancementManager.get("").getName());
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
