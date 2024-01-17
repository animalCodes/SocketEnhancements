package net.wandermc.enhancements.config;

import java.util.EnumMap;

import org.bukkit.Material;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public class Settings {
  public static final String NAMESPACE = "socketenhancements";
  // Lore is italic by default, explicitly setting it to "false" overrides this.
  public static final TextComponent EMPTY_SOCKET_MESSAGE = Component.text("<Empty Socket>").style(Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)));
  // TODO read from configuration file
  public static final int DEFAULT_SOCKET_LIMIT = 0;
  public static final EnumMap<Material, Integer> SOCKET_LIMITS = new EnumMap<Material, Integer>(Material.class);
  static {
    SOCKET_LIMITS.put(Material.BOW, 5);
    SOCKET_LIMITS.put(Material.CROSSBOW, 6);
    SOCKET_LIMITS.put(Material.TRIDENT, 4);
    SOCKET_LIMITS.put(Material.SHIELD, 4);
    SOCKET_LIMITS.put(Material.FISHING_ROD, 2);
    SOCKET_LIMITS.put(Material.FLINT_AND_STEEL, 2);
    SOCKET_LIMITS.put(Material.ELYTRA, 2);
    SOCKET_LIMITS.put(Material.IRON_SHOVEL, 3);
    SOCKET_LIMITS.put(Material.IRON_PICKAXE, 3);
    SOCKET_LIMITS.put(Material.IRON_AXE, 3);
    SOCKET_LIMITS.put(Material.IRON_HOE, 3);
    SOCKET_LIMITS.put(Material.IRON_SWORD, 3);
    SOCKET_LIMITS.put(Material.IRON_HELMET, 3);
    SOCKET_LIMITS.put(Material.IRON_CHESTPLATE, 3);
    SOCKET_LIMITS.put(Material.IRON_LEGGINGS, 3);
    SOCKET_LIMITS.put(Material.IRON_BOOTS, 3);
    SOCKET_LIMITS.put(Material.GOLDEN_SHOVEL, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_PICKAXE, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_AXE, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_HOE, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_SWORD, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_HELMET, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_CHESTPLATE, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_LEGGINGS, 6);
    SOCKET_LIMITS.put(Material.GOLDEN_BOOTS, 6);
    SOCKET_LIMITS.put(Material.DIAMOND_SHOVEL, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_PICKAXE, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_AXE, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_HOE, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_SWORD, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_HELMET, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_CHESTPLATE, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_LEGGINGS, 5);
    SOCKET_LIMITS.put(Material.DIAMOND_BOOTS, 5);
    SOCKET_LIMITS.put(Material.NETHERITE_SHOVEL, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_PICKAXE, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_AXE, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_HOE, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_SWORD, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_HELMET, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_CHESTPLATE, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_LEGGINGS, 6);
    SOCKET_LIMITS.put(Material.NETHERITE_BOOTS, 6);
    SOCKET_LIMITS.put(Material.CHAINMAIL_HELMET, 3);
    SOCKET_LIMITS.put(Material.CHAINMAIL_CHESTPLATE, 3);
    SOCKET_LIMITS.put(Material.CHAINMAIL_LEGGINGS, 3);
    SOCKET_LIMITS.put(Material.CHAINMAIL_BOOTS, 3);
    SOCKET_LIMITS.put(Material.LEATHER_HELMET, 2);
    SOCKET_LIMITS.put(Material.LEATHER_CHESTPLATE, 2);
    SOCKET_LIMITS.put(Material.LEATHER_LEGGINGS, 2);
    SOCKET_LIMITS.put(Material.LEATHER_BOOTS, 2);
  }
}
