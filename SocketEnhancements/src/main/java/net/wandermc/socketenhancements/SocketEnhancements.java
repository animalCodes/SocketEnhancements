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
package net.wandermc.socketenhancements;

import org.bukkit.plugin.java.JavaPlugin;

import net.wandermc.socketenhancements.binding.OrbOfBindingManager;
import net.wandermc.socketenhancements.commands.*;
import net.wandermc.socketenhancements.enhancement.EnhancementGemManager;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementTableManager;
import net.wandermc.socketenhancements.enhancements.*;
import net.wandermc.socketenhancements.gear.EnhancedItemForge;

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
        this.enhancementManager = new EnhancementManager(this);
        this.enhancedItemForge = new EnhancedItemForge(enhancementManager);

        registerEnhancements();
        enhancementManager.activateEnhancements();

        getCommand("addsocket").setExecutor(new AddSocketCommand(enhancedItemForge));
        getCommand("bind").setExecutor(new BindCommand(enhancementManager, enhancedItemForge));

        this.orbOfBindingManager = new OrbOfBindingManager(this, enhancedItemForge);
        this.enhancementTableManager = new EnhancementTableManager(this, enhancementManager, enhancedItemForge);
        this.enhancementGemManager = new EnhancementGemManager(this, enhancementManager, enhancedItemForge);
    }

    /**
     * Registers all SocketEnhancements core enhancements.
     */
    private void registerEnhancements() {
        enhancementManager.store(new Protected(enhancedItemForge));
    }
}
