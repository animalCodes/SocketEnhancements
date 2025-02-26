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
package net.wandermc.socketenhancements;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.wandermc.socketenhancements.binding.OrbOfBindingManager;
import net.wandermc.socketenhancements.commands.*;
import net.wandermc.socketenhancements.enhancement.*;
import net.wandermc.socketenhancements.item.EnhancedItemForge;

/**
 * SocketEnhancements: a gear enhancement plugin for PaperMC servers.
 */
public class SocketEnhancements extends JavaPlugin {
    private EnhancementManager enhancementManager;
    private EnhancedItemForge enhancedItemForge;

    private OrbOfBindingManager orbOfBindingManager;
    private EnhancementTableManager enhancementTableManager;
    private EnhancementGemManager enhancementGemManager;

    public void onEnable() {
        // TODO stop this from logging a warning if the file already exists
        saveResource("sockets.yml", false);
        YamlConfiguration socketsConfig = YamlConfiguration
            .loadConfiguration(new File(getDataFolder(), "sockets.yml"));

        saveResource("enhancements.yml", false);
        YamlConfiguration enhancementsConfig = YamlConfiguration
            .loadConfiguration(new File(getDataFolder(), "enhancements.yml"));

        this.enhancementManager = new EnhancementManager(this,
            new EmptySocket(socketsConfig));
        this.enhancedItemForge = new EnhancedItemForge(this,
            enhancementManager, socketsConfig);

        registerEnhancements(nsConfig(enhancementsConfig
            .getConfigurationSection("enhancements")));

        getCommand("sea").setExecutor(
            new SeaCommand(enhancementManager, enhancedItemForge));

        ConfigurationSection orbsConfig = nsConfig(socketsConfig
            .getConfigurationSection("orbs_of_binding"));
        if (orbsConfig.getBoolean("enabled", true))
            this.orbOfBindingManager = new OrbOfBindingManager(this,
                enhancedItemForge, orbsConfig);

        ConfigurationSection tablesConfig = nsConfig(enhancementsConfig
            .getConfigurationSection("enhancement_tables"));
        if (tablesConfig.getBoolean("enabled", true))
            this.enhancementTableManager = new EnhancementTableManager(this,
                enhancementManager, enhancedItemForge, tablesConfig);

        ConfigurationSection gemConfig = nsConfig(enhancementsConfig
            .getConfigurationSection("enhancement_gems"));
        if (gemConfig.getBoolean("enabled", true))
            this.enhancementGemManager = new EnhancementGemManager(this,
                enhancedItemForge, gemConfig);
    }

    /**
     * Register all SocketEnhancements core enhancements.
     *
     * @param config enhancements configuration section.
     */
    private void registerEnhancements(ConfigurationSection config) {
        ConfigurationSection protectedConfig = nsConfig(config
            .getConfigurationSection("protected"));
        if (protectedConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new ProtectedEnhancement(enhancedItemForge));

        ConfigurationSection blinkConfig = nsConfig(config
            .getConfigurationSection("blink"));
        if (blinkConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new BlinkEnhancement(enhancedItemForge, blinkConfig));

        ConfigurationSection boostConfig = nsConfig(config
            .getConfigurationSection("boost"));
        if (boostConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new BoostEnhancement(enhancedItemForge, boostConfig));

        ConfigurationSection cushioningConfig = nsConfig(config
            .getConfigurationSection("cushioning"));
        if (cushioningConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new CushioningEnhancement(enhancedItemForge));

        ConfigurationSection directingConfig = nsConfig(config
            .getConfigurationSection("directing"));
        if (directingConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new DirectingEnhancement(enhancedItemForge));

        ConfigurationSection explosiveConfig = nsConfig(config
            .getConfigurationSection("explosive"));
        if (explosiveConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new ExplosiveEnhancement(enhancedItemForge));

        ConfigurationSection scorchingConfig = nsConfig(config
            .getConfigurationSection("scorching"));
        if (scorchingConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new ScorchingEnhancement(enhancedItemForge, scorchingConfig));

        ConfigurationSection lifestealConfig = nsConfig(config
            .getConfigurationSection("lifesteal"));
        if (lifestealConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new LifestealEnhancement(enhancedItemForge, lifestealConfig));

        ConfigurationSection frigidConfig = nsConfig(config
            .getConfigurationSection("frigid"));
        if (frigidConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new FrigidEnhancement(enhancedItemForge));

        ConfigurationSection witheringConfig = nsConfig(config
            .getConfigurationSection("withering"));
        if (witheringConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new WitheringEnhancement(enhancedItemForge, witheringConfig));

        ConfigurationSection undyingConfig = nsConfig(config
            .getConfigurationSection("undying"));
        if (undyingConfig.getBoolean("enabled", true))
            enhancementManager.register(
                new UndyingEnhancement(enhancedItemForge));
    }

    /**
     * Replace `config` with an empty section if null.
     *
     * @param config Configuration section to check.
     * @return Null-safe config.
     */
    private ConfigurationSection nsConfig(ConfigurationSection config) {
        if (config == null)
            // YamlConfiguration appears to be the only extension of
            // ConfigurationSection with an accessible blank constructor.
            return new YamlConfiguration();
        return config;
    }
}
