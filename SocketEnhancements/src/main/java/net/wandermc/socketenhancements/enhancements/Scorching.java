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
package net.wandermc.socketenhancements.enhancements;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.enhancement.ActiveEnhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Scorching enhancement, knocks back attackers and sets them on fire for a
 * brief period, scales with each enhanced armour piece.
 * Think thorns + fire aspect.
 */
public class Scorching implements ActiveEnhancement<EntityDamageByEntityEvent> {
    // How many fire ticks to apply to the attacker per enhanced armour piece.
    private static final int FIRE_TICKS = 20;
    // Knockback strength applied to attacker for each enhanced armour piece.
    private static final double KNOCKBACK = 0.25;

    private EnhancedItemForge forge;

    /**
     * Create a Scorching enhancement
     * 
     * @param forge The current EnhancedItemForge
     */
    public Scorching(EnhancedItemForge forge) {
        this.forge = forge;
    }

    public boolean run(EntityDamageByEntityEvent context) {
        if (context.getEntity() instanceof LivingEntity defender) {
            if (context.getDamager() instanceof LivingEntity attacker) {
                for (ItemStack armourPiece : defender.getEquipment()
                    .getArmorContents()) {
                    if (armourPiece == null ||
                        armourPiece.getType() == Material.AIR)
                        continue;

                    if (forge.create(armourPiece).hasEnhancement(this)) {
                        attacker.setFireTicks(
                            attacker.getFireTicks()+FIRE_TICKS);
                        attacker.knockback(KNOCKBACK,
                            // Find RELATIVE positon, so if defender is at x=70
                            // and attacker is at x=71, we want -1.
                            (defender.getX() == attacker.getX() ? 0 :
                            defender.getX() - attacker.getX()),
                            (defender.getZ() == attacker.getZ() ? 0 :
                            defender.getZ() - attacker.getZ()));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return "scorching";
    }

    public TextComponent getSocketMessage() {
        // "<Scorching>" where the text "Scorching" is yellow and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Scorching", NamedTextColor.YELLOW))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        String type = item.getItemStack().getType().toString();
        return type.contains("HELMET") || type.contains("CHESTPLATE") || type.
            contains("LEGGINGS") || type.contains("BOOTS");
    }

    public Class<EntityDamageByEntityEvent> getEventType() {
        return EntityDamageByEntityEvent.class;
    }
}
