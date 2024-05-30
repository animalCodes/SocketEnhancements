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

import net.wandermc.socketenhancements.enhancement.EmptySocket;
import net.wandermc.socketenhancements.enhancement.Enhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;

/**
 * /bind: the default, operator-only method of binding an enhancement to an item.
 */
public class BindCommand implements CommandExecutor {
    private EnhancementManager enhancementManager;
    private EnhancedItemForge forge;

    /**
     * Create a BindCommand
     *
     * @param manager The current EnhancementManager
     */
    public BindCommand(EnhancementManager manager, EnhancedItemForge forge) {
        this.enhancementManager = manager;
        this.forge = forge;
    }

    /**
     * Attempts to bind enhancement to item, notifying sender of any issues.
     *
     * This does NOT update the item.
     *
     * @param sender The player doing the binding.
     * @param item The item being modified.
     * @param enhancement The enhancement to bind.
     * @return Whether the binding was successful.
     */
    private boolean bind(CommandSender sender, EnhancedItem item,
        Enhancement enhancement) {
            if (!item.hasEmptySocket()) {
                sender.sendMessage(
                    Component.text("No empty sockets available."));
                return false;
            }

            if (!enhancement.isValidItem(item)) {
                sender.sendMessage(Component.text("\"" + enhancement.getName() +
                    "\" cannot be bound to this item."));
                return false;
            }

            if (item.hasEnhancement(enhancement)) {
                sender.sendMessage(Component.text("This item already has \"" +
                        enhancement.getName() + "\"."));
                return false;
            }

            // EnhancedItem.bind() also does most of the above checks, oh well.
            item.bind(enhancement);

            sender.sendMessage(Component.text("Bound \"" +
                enhancement.getName() + "\" to held item."));

            return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (player.getInventory().getItemInMainHand().isEmpty()) {
                sender.sendMessage(
                    Component.text("Can't bind an enhancement to nothing!"));
                return true;
            }

            EnhancedItem item = forge.create(player.getInventory().getItemInMainHand());

            if (args.length < 1) {
                sender.sendMessage(Component.text("No enhancement given."));
                sender.sendMessage(
                    Component.text("Trying all registered enhancements..."));

                for (Enhancement enhancement : enhancementManager.getAll()) {
                        if (bind(sender, item, enhancement)) {
                            item.update();
                            return true;
                        }
                }

                sender.sendMessage(
                    Component.text("Couldn't find a valid enhancement."));

                return true;
            }

            for (int i = 0; i < args.length; i++) {
                Enhancement enhancement = enhancementManager.get(args[i]);
                if (enhancement instanceof EmptySocket) {
                    sender.sendMessage(Component.text("Unknown enhancement \"" +
                        args[0] + "\""));
                    return false;
                }
                bind(sender, item, enhancement);
            }

            item.update();

            return true;
        } else {
            sender.sendMessage(Component.text("Only players can run this command."));
            return true;
        }
    }
}

