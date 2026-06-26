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

import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.EquipmentSlot;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;

import static io.papermc.paper.tag.BaseTag.ITEMS_HEAD_ARMOR;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Glowing enhancement.
 *
 * While wearing an enhanced helmet, give player night vision.
 */
public class GlowingEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<yellow>Glowing<white>>");

    private final PotionEffect nightVision = new PotionEffect(
        PotionEffectType.NIGHT_VISION, -1, 1, true, false, false);

    private EnhancedItemForge forge;

    /**
     * Create a Glowing enhancement.
     *
     * @param forge The current EnhancedItemForge.
     */
    public GlowingEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    @EventHandler(ignoreCancelled=true)
    public void run(EntityEquipmentChangedEvent context) {
        EntityEquipmentChangedEvent.EquipmentChange change =
            context.getEquipmentChanges().get( EquipmentSlot.HEAD);
        if (change == null)
            return;

        ItemStack helmet = change.newItem();

        if (helmet.isEmpty() || !forge.has(helmet, this)) {
            // If new helmet does not have this and old helmet *did*, remove the
            // effect.
            ItemStack old = change.oldItem();

            if (!old.isEmpty() && forge.has(old, this)) {
                context.getEntity().removePotionEffect(
                    PotionEffectType.NIGHT_VISION);
            }
        } else {
            // If new helmet does have this enhancement, give wearer effect.
            context.getEntity().addPotionEffect(nightVision);
        }
    }

    public String name() {
        return "glowing";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.II;
    }

    public boolean isValidItem(EnhancedItem item) {
        return ITEMS_HEAD_ARMOR.isTagged(item.itemStack().getType());
    }
}
