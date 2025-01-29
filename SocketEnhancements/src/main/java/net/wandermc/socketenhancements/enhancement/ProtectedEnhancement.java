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
package net.wandermc.socketenhancements.enhancement;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Protected enhancement, Stops the item from breaking but will be consumed in
 * the process.
 */
public class ProtectedEnhancement implements Enhancement, Listener {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<dark_gray>Protected<white>>");

    private EnhancedItemForge forge;

    /**
     * Create a Protected enhancement
     * 
     * @param forge The current EnhancedItemForge
     */
    public ProtectedEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    @EventHandler
    public void run(PlayerItemBreakEvent context) {
        EnhancedItem enhancedItem = forge.create(context.getBrokenItem());

        if (!enhancedItem.hasEnhancement(this))
            return;

        enhancedItem.removeEnhancement(this);

        ItemStack itemStack = enhancedItem.update();

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof Damageable))
            return; // Technically this should never happen, as
                    // undamageable items cannot have this enhancement.

        ((Damageable) itemMeta).setDamage(0);
        itemStack.setItemMeta(itemMeta);

        context.getPlayer().getInventory().addItem(itemStack);
    }

    public String getName() {
        return "protected";
    }

    public TextComponent getSocketMessage() {
        return socketMessage;
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        // If an item can take damage, it can break.
        // Unless it's an elytra..
        return item.getItemStack().getItemMeta() instanceof Damageable
            && item.getItemStack().getType() != Material.ELYTRA;
    }
    public Class<PlayerItemBreakEvent> getEventType() {
        return PlayerItemBreakEvent.class;
    }
}
