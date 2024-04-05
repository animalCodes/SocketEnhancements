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

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Generic configuration related to enhancements, by default read from enhancements.yml.
 */
public class EnhancementsConfig {
    /**
     * Whether enchanting tables can be used to enhance items.
     */
    public final boolean ENHANCEMENT_TABLES_ENABLED;
    /**
     * Create a SocketsConfig with values read from `file`.
     *
     * @param file The .yml file to read from.
     */
    public EnhancementsConfig(File file) {
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection enhancementTablesSection = yamlConfig.getConfigurationSection("enhancement_tables");

        this.ENHANCEMENT_TABLES_ENABLED = enhancementTablesSection.getBoolean("enabled", true);
    }
}
