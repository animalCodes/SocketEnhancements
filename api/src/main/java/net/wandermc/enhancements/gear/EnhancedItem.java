package net.wandermc.enhancements.gear;

import java.util.ArrayList;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;

import net.wandermc.enhancements.config.Settings;

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
   * Gets how many sockets are currently on the item.
   *
   * @return The number of sockets
   */
  public int getSockets() {
    return socketList.size();
  }

  /**
   * Gets how many empty sockets are currently on the item.
   *
   * @return The number of empty sockets
   */
  public int getEmptySockets() {
    int count = 0;
    for (String socket : socketList)
      if (socket.isEmpty())
        count++;
    return count;
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
    // Get lore list
    ArrayList<Component> lore;
    if (itemMeta.hasLore())
      lore = (ArrayList<Component>) itemMeta.lore();
    else
      lore = new ArrayList<Component>();

    for (int i = 0; i < sockets; i++) {
      socketList.add("");
      lore.add(Settings.EMPTY_SOCKET_MESSAGE);
    }

    itemMeta.lore(lore);
  }

  /**
   * Gets the ItemStack held by this instance.
   * 
   * @return The item
   */
  public ItemStack getItemStack() {
    itemMeta.getPersistentDataContainer().set(socketsKey, PersistentDataType.LIST.strings(), socketList);
    item.setItemMeta(itemMeta);
    return item;
  }
}
