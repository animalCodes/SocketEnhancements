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
                return false;
            }

            EnhancedItem item = new EnhancedItem(enhancementManager, player.getInventory().getItemInMainHand());

            if (!item.hasEmptySocket()) {
                sender.sendMessage(Component.text("No empty sockets available."));
                return false;
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
                return false;
            }

            if (item.hasEnhancement(enhancement)) {
                sender.sendMessage(Component.text("This item already has that enhancement."));
                return false;
            }

            // EnhancedItem.bind() also does most of the above checks, oh well.
            item.bind(enhancement);

            player.getInventory().setItemInMainHand(item.getItemStack());

            return true;
        } else {
            sender.sendMessage(Component.text("Only players can run this command."));
            return false;
        }
    }
}

