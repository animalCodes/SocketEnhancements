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
package net.wandermc.socketenhancements.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import net.wandermc.socketenhancements.enhancement.EmptySocket;
import net.wandermc.socketenhancements.enhancement.Enhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;

/**
 * /sea: SocketEnhancements administration command, has various subcommands.
 *
 * Subcommands:
 * - bind {enhancements} - Bind `enhancements` to held item.
 * - addsocket {n} - Add `n` sockets to held item.
 * - replace {enhancement1} {enhancement2} - Replace `enhancement1` with
 *   `enhancement2` on held item.
 * - help - Print help.
 */
public class SeaCommand implements CommandExecutor {
    private EnhancementManager enhancementManager;
    private EnhancedItemForge forge;

    private static TextComponent[] subCommands = {
        Component.text("bind {enhancements} - Bind given list of enhancements" +
        " to item held in main hand. If no enhancements are given" +
        " SocketEnhancements will bind the first valid enhancement it finds."),
        Component.text("addsocket {n} - Add n socket(s) to item held in main" +
        " hand. If n isn't specified, 1 socket is added."),
        Component.text("replace {enhancement1} {enhancement2} - Replace" +
        " enhancement1 with enhancement2 on item held in main hand." +
        " enhancement1 may be an unregistered enhancement, in which case" +
        " this can be used to update items after an enhancement's name has" +
        " been changed."),
        Component.text("help - Print this help.")
    };

    /**
     * Create a SeaCommand.
     *
     * @param forge The current EnhancedItemForge.
     * @param manager The current EnhancementManager.
     */
    public SeaCommand(EnhancementManager manager, EnhancedItemForge forge) {
        this.enhancementManager = manager;
        this.forge = forge;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
        String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("No subcommand specified."));
            sender.sendMessage(Component.text("Valid subcommands:"));
            return helpCommand(sender);
        }

        if (sender instanceof Player player) {
            switch (args[0].toLowerCase()) {
                case "bind":
                    return bindCommand(player, args);
                case "addsocket":
                    return addSocketCommand(player, args);
                case "replace":
                    return replaceCommand(player, args);
                case "help":
                default:
                    return helpCommand(sender);
            }
        } else {
            sender.sendMessage(
                Component.text("Only players can use this command."));
            return true;
        }
    }

    private boolean bindCommand(Player sender, String[] args) {
        if (sender.getInventory().getItemInMainHand().isEmpty()) {
            sender.sendMessage(
                Component.text("Can't bind an enhancement to nothing!"));
            return true;
        }

        EnhancedItem item = forge.create(sender.getInventory()
            .getItemInMainHand());

        if (args.length < 2) {
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

        for (int i = 1; i < args.length; i++) {
            Enhancement enhancement = enhancementManager.get(args[i]);
            if (enhancement instanceof EmptySocket) {
                sender.sendMessage(Component.text("Unknown enhancement \"" +
                    args[i] + "\""));
                return false;
            }

            bind(sender, item, enhancement);
        }

        item.update();

        return true;
    }

    private boolean addSocketCommand(Player sender, String[] args) {
        if (sender.getInventory().getItemInMainHand().isEmpty()) {
            sender.sendMessage(
                Component.text("Can't add a socket to nothing!"));
            return true;
        }

        EnhancedItem item = forge.create(sender.getInventory()
            .getItemInMainHand());

        if (item.sockets() >= item.socketLimit()) {
            sender.sendMessage(Component.text(
                "This item already has the maximum number of sockets. (" +
                 item.socketLimit() + ")"));
            return true;
        }

        int numSockets = 1;
        if (args.length > 1) {
            try {
                numSockets = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text(args[1] +
                    " Is not a valid number."));
                return false;
            }
        }

        if (item.sockets() + numSockets > item.socketLimit()) {
            sender.sendMessage(Component.text("Adding that many " +
                "sockets would put this item over it's socket limit. (" +
                 item.socketLimit() + ")"));
            return true;
        }

        item.addSockets(numSockets);
        item.update();

        sender.sendMessage(Component.text("Added " + numSockets +
            " to held item."));

        return true;
    }

    private boolean replaceCommand(Player sender, String[] args) {
        if (sender.getInventory().getItemInMainHand().isEmpty()) {
            sender.sendMessage(
                Component.text("No item in main hand."));
            return true;
        }

        EnhancedItem item = forge.create(sender.getInventory()
            .getItemInMainHand());

        if (args.length < 3) {
            if (args.length < 2)
                sender.sendMessage(Component.text("No base or replacement" +
                    " enhancement given."));
            else
                sender.sendMessage(Component.text("No replacement" +
                    " enhancement given."));
            return false;
        }

        Enhancement enhancement2 = enhancementManager.get(args[2]);
        if (enhancement2 instanceof EmptySocket) {
            sender.sendMessage(Component.text("Unknown enhancement \"" +
                args[2] + "\""));
            return false;
        }

        if (!item.remove(args[1])) {
            sender.sendMessage(Component.text("Item doesn't have \"" +
                args[1] + "\" bound to it."));
            return false;
        }

        if (!item.bind(enhancement2)) {
            sender.sendMessage(Component.text("\"" + args[2] +
                "\" cannot be bound to item."));
            return false;
        }

        item.update();

        return true;
    }

    private boolean helpCommand(CommandSender sender) {
        for (TextComponent message : subCommands)
            sender.sendMessage(message);
        return true;
    }

    /**
     * Attempt to bind enhancement to item, notifying sender of any issues.
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
            sender.sendMessage(Component.text("\"" + enhancement.name() +
                "\" cannot be bound to this item."));
            return false;
        }

        if (item.has(enhancement)) {
            sender.sendMessage(Component.text("This item already has \"" +
                    enhancement.name() + "\"."));
            return false;
        }

        // EnhancedItem.bind() also does most of the above checks, oh well.
        item.bind(enhancement);

        sender.sendMessage(Component.text("Bound \"" +
            enhancement.name() + "\" to held item."));

        return true;
    }
}
