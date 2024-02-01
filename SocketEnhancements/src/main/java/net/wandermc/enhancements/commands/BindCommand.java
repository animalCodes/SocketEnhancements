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
import net.wandermc.enhancements.enhancement.Enhancement;
import net.wandermc.enhancements.enhancement.EnhancementManager;

public class BindCommand implements CommandExecutor {
    private EnhancementManager enhancementManager;

    public BindCommand(EnhancementManager manager) {
        this.enhancementManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (player.getInventory().getItemInMainHand().isEmpty()) {
                sender.sendMessage(Component.text("Can't bind an enhancement to nothing!"));
                return true;
            }

            EnhancedItem item = new EnhancedItem(enhancementManager, player.getInventory().getItemInMainHand());

            if (!item.hasEmptySocket()) {
                sender.sendMessage(Component.text("No empty sockets available."));
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(Component.text("No enhancement given."));
                return false;
            }

            Enhancement enhancement = enhancementManager.get(args[0]);
            if (enhancement == null) {
                sender.sendMessage(Component.text("Invalid enhancement \"" + args[0] + "\""));
                return false;
            }

            if (!enhancement.isValidItem(item)) {
                sender.sendMessage(Component.text("\"" + args[0] + "\" cannot be bound to this item."));
                return true;
            }

            if (item.hasEnhancement(enhancement)) {
                sender.sendMessage(Component.text("This item already has that enhancement."));
                return true;
            }

            // EnhancedItem.bind() also does most of the above checks, oh well.
            item.bind(enhancement);
            player.getInventory().setItemInMainHand(item.getItemStack());

            sender.sendMessage(Component.text("Bound " + enhancement.getName() + " to held item."));

            return true;
        } else {
            sender.sendMessage(Component.text("Only players can run this command."));
            return true;
        }
    }
}

