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
package net.wandermc.socketenhancements.config;

import java.util.EnumMap;

import org.bukkit.Material;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Class for storing values that may need to be accessed anywhere in the api and that won't change during runtime.
 */
public class Settings {
    // TODO store under plugin namespace (use plugin instance)
    /**
     * The namespace used by the api
     */
    public static final String NAMESPACE = "socketenhancements";

    /**
     * The name of Enhancement Gems
     */
    public static final TextComponent ENHANCEMENT_GEM_NAME = Component.text("Enhancement Gem", 
            Style.style(TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)));
    /**
     * The type of Enhancement Gems
     */
    public static final Material ENHANCEMENT_GEM_TYPE = Material.END_CRYSTAL;

    /**
     * The message displayed on an item's lore for each empty socket it has.
     */
    public static final TextComponent EMPTY_SOCKET_MESSAGE = Component.text("<Empty Socket>")
            // Lore is italic by default, explicitly setting it to "false" overrides this.
            .style(Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)));

    // TODO read from configuration file
    /**
     * Socket limit used for any item not in SOCKET_LIMITS
     */
    public static final int DEFAULT_SOCKET_LIMIT = 0;
    /**
     * Limits for how many sockets can be applied to certain items.
     * By default, only "gear" items can be enhanced.
     */
    public static final EnumMap<Material, Integer> SOCKET_LIMITS = new EnumMap<Material, Integer>(Material.class);
    static {
        SOCKET_LIMITS.put(ENHANCEMENT_GEM_TYPE, 1);
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
