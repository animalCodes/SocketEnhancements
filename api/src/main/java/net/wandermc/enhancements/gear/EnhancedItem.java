package net.wandermc.enhancements.gear;

import java.util.ArrayList;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;

import net.wandermc.enhancements.config.Settings;
import net.wandermc.enhancements.enhancement.Enhancement;
import net.wandermc.enhancements.enhancement.EnhancementManager;

public class EnhancedItem {
    private static NamespacedKey socketsKey = new NamespacedKey(Settings.NAMESPACE, "sockets");

    private ItemStack item;
    private ItemMeta itemMeta;
    private ArrayList<String> socketList;

    public EnhancedItem(ItemStack item) {
        this.item = item;
        this.itemMeta = item.getItemMeta();

        PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        if (!dataContainer.has(socketsKey))
            dataContainer.set(socketsKey, PersistentDataType.LIST.strings(), new ArrayList<String>());
        this.socketList = new ArrayList<String>(dataContainer.get(socketsKey, PersistentDataType.LIST.strings()));
    }

    /**
     * Updates item lore to match socketList.
     * Essentially converts the unique name of the Enhancement to it's socket
     * message.
     */
    private void updateLore() {
        ArrayList<Component> lore = new ArrayList<Component>(socketList.size());

        socketList.forEach(socketId -> lore.add(EnhancementManager.get(socketId).getSocketMessage()));

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
        return hasEnhancement(EnhancementManager.get(""));
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
     * @param Sockets The number of sockets to add
     */
    public void addSockets(int sockets) {
        for (int i = 0; i < sockets; i++) {
            // Can't use `bind()` as unlike normal enhancements empty sockets can very much
            // be 'bound' multiple times.
            socketList.add(EnhancementManager.get("").getName());
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

        int index = socketList.indexOf(EnhancementManager.get("").getName());
        if (index < 0)
            // No empty sockets
            return false;
        else
            // Yes empty sockets, fill it
            socketList.set(index, enhancement.getName());

        return true;
    }

    /**
     * Gets the ItemStack held by this instance.
     * 
     * @return The item
     */
    public ItemStack getItemStack() {
        itemMeta.getPersistentDataContainer().set(socketsKey, PersistentDataType.LIST.strings(), socketList);
        updateLore();
        item.setItemMeta(itemMeta);
        return item;
    }
}
