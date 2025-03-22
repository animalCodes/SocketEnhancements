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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
public class SeaCommand implements TabExecutor {
    // 'Informational' text is coloured YELLOW. Errors are coloured RED.
    private static final Component noSubCommandMsg = Component.text(
        "No subcommand specified.").color(NamedTextColor.RED).appendNewline()
        .append(Component.text("Valid subcommands:")
            .color(NamedTextColor.YELLOW));
    private static final Component onlyPlayersMsg = Component.text(
        "Only players can use this command.").color(NamedTextColor.RED);
    private static final Component bindHelpMsg = Component.text(
        "bind {enhancements} - Bind given list of enhancements to item held " +
        "in main hand.").color(NamedTextColor.YELLOW);
    private static final Component addsocketHelpMsg = Component.text(
        "addsocket {n} - Add n socket(s) to item held in main hand. If n " +
        "isn't specified, 1 socket is added.").color(NamedTextColor.YELLOW);
    private static final Component replaceHelpMsg = Component.text(
        "replace {enhancement1} {enhancement2} - Replace enhancement1 with " +
        "enhancement2 on item held in main hand. enhancement1 may be an " +
        "unregistered enhancement, in which case this can be used to update " +
        "items after an enhancement's name has been changed.")
        .color(NamedTextColor.YELLOW);
    private static final Component helpHelpMsg = Component.text(
        "help - Print this help.").color(NamedTextColor.YELLOW);

    private static final Component noItemMsg = Component.text(
        "No item in main hand.").color(NamedTextColor.RED);
    private static final Component cannotBindMsgEnd = Component.text(
        " cannot be bound to this item.").color(NamedTextColor.RED);
    private static final Component unknownEnhancementMsgStart = Component.text(
        "Unknown enhancement ").color(NamedTextColor.RED);

    private static final Component noEnhancementMsg = Component.text(
        "No enhancement given.").color(NamedTextColor.RED);
    private static final Component noEmptySocketsMsg = Component.text(
        "No empty sockets available.").color(NamedTextColor.RED);
    private static final Component alreadyBoundMsgStart = Component.text(
        "This item already has ").color(NamedTextColor.RED);

    private static final Component maxSocketsMsgStart = Component.text(
        "This item already has the maximum number of sockets. ")
        .color(NamedTextColor.RED);
    private static final Component badNumberMsgEnd = Component.text(
        " Is not a valid number.").color(NamedTextColor.RED);
    private static final Component tooManySocketsMsgStart = Component.text(
        "Adding that many sockets would put this item over its socket limit. ")
        .color(NamedTextColor.RED);

    private static final Component noBaseOrReplacementMsg = Component.text(
        "No base or replacement enhancement given.").color(NamedTextColor.RED);
    private static final Component noReplacementMsg = Component.text(
        "No replacement enhancement given.").color(NamedTextColor.RED);
    private static final Component notBoundMsgStart = Component.text(
        "Item doesn't have ").color(NamedTextColor.RED);
    private static final Component notBoundMsgEnd = Component.text(
        " bound to it.").color(NamedTextColor.RED);


    private EnhancementManager enhancementManager;
    private EnhancedItemForge forge;

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
        if (sender instanceof Player player) {
            if (args.length < 1) {
                sender.sendMessage(noSubCommandMsg);
                helpCommand(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "bind":
                    bindCommand(player, args);
                    break;
                case "addsocket":
                    addSocketCommand(player, args);
                    break;
                case "replace":
                    replaceCommand(player, args);
                    break;
                case "help":
                default:
                    helpCommand(sender);
            }
        } else {
            sender.sendMessage(onlyPlayersMsg);
        }

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command,
        String label, String[] args) {
        ArrayList<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("bind");
            suggestions.add("addsocket");
            suggestions.add("replace");
            suggestions.add("help");
            return suggestions;
        }

        switch (args[0].toLowerCase()) {
            case "bind": {
                suggestions.addAll(enhancementManager.getAllNames());
                break;
            } case "addsocket": {
                suggestions.add("1");
                break;
            } case "replace": {
                suggestions.addAll(enhancementManager.getAllNames());
                break;
            }
        }

        return suggestions;
    }

    private void bindCommand(Player sender, String[] args) {
        if (sender.getInventory().getItemInMainHand().isEmpty()) {
            sender.sendMessage(noItemMsg);
            return;
        }

        EnhancedItem item = forge.create(sender.getInventory()
            .getItemInMainHand());

        if (args.length < 2) {
            sender.sendMessage(noEnhancementMsg);
            return;
        }

        for (int i = 1; i < args.length; i++) {
            Enhancement enhancement = enhancementManager.get(args[i]);
            if (enhancement instanceof EmptySocket) {
                sender.sendMessage(unknownEnhancementMsgStart.append(
                    Component.text('"'+args[i]+'"')));
                return;
            }

            bind(sender, item, enhancement);
        }

        item.update();
    }

    private void addSocketCommand(Player sender, String[] args) {
        if (sender.getInventory().getItemInMainHand().isEmpty()) {
            sender.sendMessage(noItemMsg);
            return;
        }

        EnhancedItem item = forge.create(sender.getInventory()
            .getItemInMainHand());

        int numSockets = 1;
        if (args.length > 1) {
            try {
                numSockets = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text('"'+args[1]+'"')
                    .color(NamedTextColor.RED).append(badNumberMsgEnd));
                return;
            }
        }

        if (item.sockets() >= item.socketLimit()) {
            sender.sendMessage(maxSocketsMsgStart.append(
                Component.text("("+item.socketLimit()+")")));
            return;
        }

        if (item.sockets() + numSockets > item.socketLimit()) {
            sender.sendMessage(tooManySocketsMsgStart.append(
                Component.text("("+item.socketLimit()+")")));
            return;
        }

        item.addSockets(numSockets);
        item.update();
    }

    private void replaceCommand(Player sender, String[] args) {
        if (sender.getInventory().getItemInMainHand().isEmpty()) {
            sender.sendMessage(noItemMsg);
            return;
        }

        EnhancedItem item = forge.create(sender.getInventory()
            .getItemInMainHand());

        if (args.length < 3) {
            if (args.length < 2)
                sender.sendMessage(noBaseOrReplacementMsg);
            else
                sender.sendMessage(noReplacementMsg);
            return;
        }

        Enhancement enhancement2 = enhancementManager.get(args[2]);
        if (enhancement2 instanceof EmptySocket) {
            sender.sendMessage(unknownEnhancementMsgStart.append(
                Component.text('"'+args[2]+'"')));
            return;
        }

        if (!item.remove(args[1])) {
            sender.sendMessage(notBoundMsgStart.append(
                Component.text('"'+args[1]+'"')).append(notBoundMsgEnd));
            return;
        }

        if (!item.bind(enhancement2)) {
            sender.sendMessage(Component.text('"'+args[2]+'"')
                .color(NamedTextColor.RED).append(cannotBindMsgEnd));
            return;
        }

        item.update();
    }

    private void helpCommand(CommandSender sender) {
        sender.sendMessage(bindHelpMsg);
        sender.sendMessage(addsocketHelpMsg);
        sender.sendMessage(replaceHelpMsg);
        sender.sendMessage(helpHelpMsg);
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
            sender.sendMessage(noEmptySocketsMsg);
            return false;
        }

        if (!enhancement.isValidItem(item)) {
            sender.sendMessage(Component.text('"'+enhancement.name()+'"')
                .color(NamedTextColor.RED).append(cannotBindMsgEnd));
            return false;
        }

        if (item.has(enhancement)) {
            sender.sendMessage(alreadyBoundMsgStart
                .append(Component.text('"'+enhancement.name()+'"')));
            return false;
        }

        item.bind(enhancement);

        return true;
    }
}
