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
package net.wandermc.socketenhancements.config;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * All configuration related to sockets, by default read from sockets.yml.
 */
public class SocketsConfig {
    /**
     * The message displayed on an item's lore for each empty socket it has.
     */
    public final TextComponent EMPTY_SOCKET_MESSAGE;

    /**
     * Socket limit used for any item not in SOCKET_LIMITS.
     */
    public final int DEFAULT_SOCKET_LIMIT;

    /**
     * Limits for how many sockets can be applied to certain items.
     */
    public final EnumMap<Material, Integer> SOCKET_LIMITS;

    /**
     * Whether Orbs of Binding can be crafted and applied to items.
     */
    public final boolean ORBS_OF_BINDING_ENABLED;

    /**
     * Ingredients used to craft an orb of binding.
     * Length must be between 1 and 9 inclusive.
     */
    public final ArrayList<Material> ORB_OF_BINDING_INGREDIENTS;

    /**
     * Material type of orbs of binding.
     */
    public final Material ORB_OF_BINDING_TYPE;

    /**
     * Create a SocketsConfig with values read from `file`.
     *
     * @param file The .yml file to read from.
     */
    public SocketsConfig(File file) {
        YamlConfiguration yamlConfig =
            YamlConfiguration.loadConfiguration(file);

        this.EMPTY_SOCKET_MESSAGE = (TextComponent) yamlConfig
            .getRichMessage("empty_socket_message",
                Component.text("<Empty Socket>"));

        int dsl = yamlConfig.getInt("default", 0);
        if (dsl < 0)
            dsl = 0;
        this.DEFAULT_SOCKET_LIMIT = dsl;

        this.SOCKET_LIMITS = new EnumMap<Material, Integer>(Material.class);

        ConfigurationSection limitsSection =
            yamlConfig.getConfigurationSection("limits");
        if (limitsSection != null) {
            for (String key : limitsSection.getKeys(false)) {
                Material material = Material.getMaterial(key);
                if (material != null && material != Material.AIR)
                    SOCKET_LIMITS.put(material, limitsSection.getInt(key));
            }
        }

        this.ORB_OF_BINDING_INGREDIENTS = new ArrayList<Material>();

        ConfigurationSection orbsOfBindingSection = yamlConfig
            .getConfigurationSection("orbs_of_binding");

        this.ORBS_OF_BINDING_ENABLED = orbsOfBindingSection
            .getBoolean("enabled", true);

        Material orbType = Material.getMaterial(orbsOfBindingSection
            .getString("material", "CONDUIT"));
        if (orbType == null || orbType == Material.AIR)
            this.ORB_OF_BINDING_TYPE = Material.CONDUIT;
        else
            this.ORB_OF_BINDING_TYPE = orbType;

        if (orbsOfBindingSection != null) {
            for (String ingredient :
                    orbsOfBindingSection.getStringList("ingredients")) {
                Material material = Material.getMaterial(ingredient);
                if (material != null && material != Material.AIR)
                    ORB_OF_BINDING_INGREDIENTS.add(material);

                if (ORB_OF_BINDING_INGREDIENTS.size() >= 9)
                    break;
            }
        }

        if (ORB_OF_BINDING_INGREDIENTS.size() == 0) {
            ORB_OF_BINDING_INGREDIENTS.add(Material.END_CRYSTAL);
            ORB_OF_BINDING_INGREDIENTS.add(Material.PRISMARINE_SHARD);
            ORB_OF_BINDING_INGREDIENTS.add(Material.CHORUS_FRUIT);
        }

        ORB_OF_BINDING_INGREDIENTS.trimToSize();
    }
}
