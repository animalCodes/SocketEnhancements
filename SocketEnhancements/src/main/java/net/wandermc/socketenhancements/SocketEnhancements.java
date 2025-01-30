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

import org.bukkit.plugin.java.JavaPlugin;

import net.wandermc.socketenhancements.config.SocketsConfig;
import net.wandermc.socketenhancements.config.EnhancementsConfig;
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
        SocketsConfig socketsConfig = new SocketsConfig(
            new File(getDataFolder(), "sockets.yml"));

        saveResource("enhancements.yml", false);
        EnhancementsConfig eConfig = new EnhancementsConfig(
            new File(getDataFolder(), "enhancements.yml"));

        this.enhancementManager = new EnhancementManager(this,
            new EmptySocket(socketsConfig.EMPTY_SOCKET_MESSAGE));
        this.enhancedItemForge = new EnhancedItemForge(this,
            enhancementManager,
            socketsConfig.SOCKET_LIMITS, socketsConfig.DEFAULT_SOCKET_LIMIT);

        registerEnhancements();
        enhancementManager.activateEnhancements();

        getCommand("sea").setExecutor(
            new SeaCommand(enhancementManager, enhancedItemForge));

        if (socketsConfig.ORBS_OF_BINDING_ENABLED)
            this.orbOfBindingManager = new OrbOfBindingManager(this,
                enhancedItemForge, socketsConfig.ORB_OF_BINDING_INGREDIENTS,
                socketsConfig.ORB_OF_BINDING_TYPE);

        if (eConfig.ENHANCEMENT_TABLES_ENABLED)
            this.enhancementTableManager = new EnhancementTableManager(this,
                enhancementManager, enhancedItemForge,
                eConfig.ENHANCEMENT_TABLES_ADDITIVE_POOLS,
                eConfig.ENHANCEMENT_TABLES_RANDOMISATION_FREQUENCY);

        if (eConfig.ENHANCEMENT_GEMS_ENABLED)
            this.enhancementGemManager = new EnhancementGemManager(this,
                enhancedItemForge, eConfig.ENHANCEMENT_GEMS_BLOCK_TYPE,
                eConfig.ENHANCEMENT_GEMS_TYPE);
    }

    /**
     * Register all SocketEnhancements core enhancements.
     */
    private void registerEnhancements() {
        enhancementManager.store(new ProtectedEnhancement(enhancedItemForge));
        enhancementManager.store(new BlinkEnhancement(enhancedItemForge));
        enhancementManager.store(new BoostEnhancement(enhancedItemForge));
        enhancementManager.store(new CushioningEnhancement(enhancedItemForge));
        enhancementManager.store(new DirectingEnhancement(enhancedItemForge));
        enhancementManager.store(new ExplosiveEnhancement(enhancedItemForge));
        enhancementManager.store(new ScorchingEnhancement(enhancedItemForge));
        enhancementManager.store(new LifestealEnhancement(enhancedItemForge));
        enhancementManager.store(new FrigidEnhancement(enhancedItemForge));
        enhancementManager.store(new IcyEnhancement(enhancedItemForge));
        enhancementManager.store(new WitheringEnhancement(enhancedItemForge));
        enhancementManager.store(new UndyingEnhancement(enhancedItemForge));
    }
}
