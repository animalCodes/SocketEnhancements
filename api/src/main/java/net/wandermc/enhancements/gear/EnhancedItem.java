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
  private PersistentDataContainer dataContainer;

  public EnhancedItem(ItemStack item) {
    this.item = item;
    this.itemMeta = item.getItemMeta();
    this.dataContainer = itemMeta.getPersistentDataContainer();

    // Avoid having to repeatedly check whether the container has a socketsKey
    // (This'll only be actually stored on the item if the api user calls
    // .getItemStack())
    if (!dataContainer.has(socketsKey))
      dataContainer.set(socketsKey, PersistentDataType.INTEGER, 0);
  }

  /**
   * Adds the specified number of sockets to this item.
   * 
   * @param Sockets The number of sockets to add
   * @return The new number of sockets on the item
   */
  public int addSockets(int sockets) {
    // Update stored number
    int currentSockets = dataContainer.get(socketsKey, PersistentDataType.INTEGER);
    dataContainer.set(socketsKey, PersistentDataType.INTEGER, currentSockets + sockets);

    // Update lore
    ArrayList<Component> lore;
    if (itemMeta.hasLore())
      lore = (ArrayList<Component>) itemMeta.lore();
    else
      lore = new ArrayList<Component>();
    for (int i = 0; i < sockets; i++)
      lore.add(Settings.EMPTY_SOCKET_MESSAGE);
    itemMeta.lore(lore);

    return currentSockets + sockets;
  }

  /**
   * Gets the ItemStack held by this instance.
   * 
   * @return The item
   */
  public ItemStack getItemStack() {
    item.setItemMeta(itemMeta);
    return item;
  }
}
