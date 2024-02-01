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
package net.wandermc.enhancements;

import org.bukkit.plugin.java.JavaPlugin;

import net.wandermc.enhancements.commands.*;
import net.wandermc.enhancements.enhancements.*;
import net.wandermc.enhancements.enhancement.EnhancementManager;

public class SocketEnhancements extends JavaPlugin {
    private EnhancementManager manager;
    
    public void onEnable() {
        this.manager = new EnhancementManager(this);

        getCommand("addsocket").setExecutor(new AddSocketCommand(manager));
        getCommand("bind").setExecutor(new BindCommand(manager));

        registerEnhancements();

        manager.activateEnhancements();
    }

    private void registerEnhancements() {
        manager.store(new Protected(manager));
    }
}
