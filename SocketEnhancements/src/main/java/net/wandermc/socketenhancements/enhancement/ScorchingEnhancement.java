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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.ARMOR;

import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Scorching enhancement, has a chance to knock back attackers and set them on
 * fire for a brief period, chance increases with each enhanced armour piece.
 * Think thorns + fire aspect.
 */
public class ScorchingEnhancement implements Enhancement, Listener {
    // Chance for effect to be applied per armour piece.
    private static final double CHANCE_PER = 0.2;
    // How many fire ticks to apply to the attacker on activation.
    private static final int FIRE_TICKS = 40;
    // Knockback strength applied to attacker on activation.
    private static final double KNOCKBACK = 0.5;

    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<yellow>Scorching<white>>");

    private EnhancedItemForge forge;

    /**
     * Create a Scorching enhancement
     * 
     * @param forge The current EnhancedItemForge
     */
    public ScorchingEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    @EventHandler
    public void run(EntityDamageByEntityEvent context) {
        if (context.getEntity() instanceof LivingEntity defender) {
            if (context.getDamager() instanceof LivingEntity attacker) {
                double chance = 0;
                for (ItemStack armourPiece : defender.getEquipment()
                    .getArmorContents()) {
                    if (armourPiece == null || armourPiece.isEmpty())
                        continue;

                    if (forge.create(armourPiece).hasEnhancement(this))
                        chance += CHANCE_PER;
                }

                if (roll(chance)) {
                    attacker.setFireTicks(attacker.getFireTicks()+FIRE_TICKS);
                    attacker.knockback(KNOCKBACK,
                        // Find RELATIVE positon, so if defender is at x=70
                        // and attacker is at x=71, we want -1.
                        (defender.getX() == attacker.getX() ? 0 :
                        defender.getX() - attacker.getX()),
                        (defender.getZ() == attacker.getZ() ? 0 :
                        defender.getZ() - attacker.getZ()));
                }
            }
        }
    }

    public String getName() {
        return "scorching";
    }

    public TextComponent getSocketMessage() {
        return socketMessage;
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        if (item.hasEnhancement("frigid"))
            return false;
        return ARMOR.isTagged(item.getItemStack().getType());
    }
}
