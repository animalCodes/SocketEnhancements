package net.wandermc.enhancements.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;

import net.wandermc.enhancements.gear.EnhancedItem;

public class AddSocketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            // For now, just assume the item is valid
            // TODO limit how many sockets an item can have
            EnhancedItem item = new EnhancedItem(player.getInventory().getItemInMainHand());

            int numSockets = 1;
            if (args.length > 0) {
                try {
                    numSockets = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text(args[0] + " Is not a valid number."));
                    return false;
                }
            }

            item.addSockets(numSockets);

            player.getInventory().setItemInMainHand(item.getItemStack());

            return true;
        }

        return false;
    }
}
