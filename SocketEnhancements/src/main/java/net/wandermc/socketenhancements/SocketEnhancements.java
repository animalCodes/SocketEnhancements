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

import net.wandermc.socketenhancements.commands.*;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancements.*;
import net.wandermc.socketenhancements.binding.OrbOfBindingManager;

/**
 * SocketEnhancements: a gear enhancement plugin for PaperMC servers.
 */
public class SocketEnhancements extends JavaPlugin {
    private EnhancementManager enhancementManager;
    private OrbOfBindingManager orbOfBindingManager;
    
    public void onEnable() {
        this.enhancementManager = new EnhancementManager(this);

        getCommand("addsocket").setExecutor(new AddSocketCommand(enhancementManager));
        getCommand("bind").setExecutor(new BindCommand(enhancementManager));

        // Simply constructing an OrbOfBindingManager is sufficient to activate orbs of binding.
        this.orbOfBindingManager = new OrbOfBindingManager(this, enhancementManager);

        registerEnhancements();
        enhancementManager.activateEnhancements();
    }

    /**
     * Registers all SocketEnhancements core enhancements.
     */
    private void registerEnhancements() {
        enhancementManager.store(new Protected(enhancementManager));
    }
}
