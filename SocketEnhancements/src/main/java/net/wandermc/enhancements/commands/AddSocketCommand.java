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
package net.wandermc.enhancements.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;

import net.wandermc.enhancements.gear.EnhancedItem;
import net.wandermc.enhancements.enhancement.EnhancementManager;

public class AddSocketCommand implements CommandExecutor {
    private EnhancementManager enhancementManager;

    public AddSocketCommand(EnhancementManager manager) {
        this.enhancementManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (player.getInventory().getItemInMainHand().isEmpty()) {
                sender.sendMessage(Component.text("Can't add a socket to nothing!"));
                return true;
            }

            EnhancedItem item = new EnhancedItem(enhancementManager, player.getInventory().getItemInMainHand());

            if (item.getSockets() >= item.getSocketLimit()) {
                sender.sendMessage(Component.text("This item already has the maximum number of sockets. (" + item.getSocketLimit() + ")"));
                return true;
            }

            int numSockets = 1;
            if (args.length > 0) {
                try {
                    numSockets = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text(args[0] + " Is not a valid number."));
                    return false;
                }
            }

            if (item.getSockets() + numSockets > item.getSocketLimit()) {
                sender.sendMessage(Component.text("Adding that many sockets would put this item over it's socket limit. (" + item.getSocketLimit() + ")"));
                return true;
            }

            item.addSockets(numSockets);
            player.getInventory().setItemInMainHand(item.getItemStack());

            sender.sendMessage(Component.text("Added " + numSockets + " to held item."));

            return true;
        } else {
            sender.sendMessage(Component.text("Only players can run this command."));
            return false;
        }
    }
}
