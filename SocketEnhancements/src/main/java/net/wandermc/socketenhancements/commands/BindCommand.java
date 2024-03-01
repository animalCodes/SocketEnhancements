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
package net.wandermc.socketenhancements.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;

import net.wandermc.socketenhancements.gear.EnhancedItem;
import net.wandermc.socketenhancements.enhancement.Enhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;

/**
 * /bind: the default, operator-only method of binding an enhancement to an item.
 */
public class BindCommand implements CommandExecutor {
    private EnhancementManager enhancementManager;

    /**
     * Create a BindCommand
     *
     * @param manager The current EnhancementManager
     */
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

            // TODO allow for multiple enhancements to be specified at once
            // TODO if no enhancements are given, pick a random one
            if (args.length < 1) {
                sender.sendMessage(Component.text("No enhancement given."));
                return false;
            }

            Enhancement enhancement = enhancementManager.get(args[0]);
            // TODO check for EmptySocket instead
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
            item.update();

            sender.sendMessage(Component.text("Bound " + enhancement.getName() + " to held item."));

            return true;
        } else {
            sender.sendMessage(Component.text("Only players can run this command."));
            return true;
        }
    }
}

