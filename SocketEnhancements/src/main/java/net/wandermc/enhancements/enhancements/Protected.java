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
package net.wandermc.enhancements.enhancements;

import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import net.wandermc.enhancements.enhancement.ActiveEnhancement;
import net.wandermc.enhancements.enhancement.EnhancementManager;
import net.wandermc.enhancements.events.EventType;
import net.wandermc.enhancements.gear.EnhancedItem;

/**
 * Protected enhancement, Stops the item from breaking but will be consumed in the process.
 */
@EventType(PlayerItemBreakEvent.class)
public class Protected implements ActiveEnhancement<PlayerItemBreakEvent> {
    private EnhancementManager manager;
    
    /**
     * Create a Protected enhancement
     * 
     * @param manager The current EnhancementManager
     */
    public Protected(EnhancementManager manager) {
        this.manager = manager;
    }

    public String getName() {
        return "protected";
    }

    public TextComponent getSocketMessage() {
        // "<Protected>" where the text "Protected" is dark gray and the "< >"s are white.
        return Component.text("<", NamedTextColor.WHITE).append(Component.text("Protected", NamedTextColor.DARK_GRAY)).append(Component.text(">", NamedTextColor.WHITE));
    }

    public boolean isValidItem(EnhancedItem item) {
        // If an item can take damage, it can break.
        return item.getItemStack().getItemMeta() instanceof Damageable;
    }

    public boolean shouldRun(PlayerItemBreakEvent context)  {
        return new EnhancedItem(manager, context.getBrokenItem()).hasEnhancement(this);
    }

    public boolean runEffect(PlayerItemBreakEvent context) {
        EnhancedItem enhancedItem = new EnhancedItem(manager, context.getBrokenItem());
        enhancedItem.removeEnhancement(this);

        ItemStack itemStack = enhancedItem.getItemStack();

        // Get and update damage
        ItemMeta itemMeta = itemStack.getItemMeta();
        // Damage = how much damage item has taken so 0 damage = full durability
        // The checker already confirmed that the item's ItemMeta is an instance of Damageable, so this should be a safe cast.
        ((Damageable) itemMeta).setDamage(0);
        itemStack.setItemMeta(itemMeta);

        context.getPlayer().getInventory().addItem(itemStack);

        return true;
    }
}
