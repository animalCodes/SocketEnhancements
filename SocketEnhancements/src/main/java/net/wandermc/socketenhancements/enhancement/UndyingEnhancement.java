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
package net.wandermc.socketenhancements.enhancement;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
 * Undying enhancement, if an entity takes fatal damage from any source other
 * than void or /kill while holding enhanced item, crudely imitate a weakened
 * totem of undying by:
 *
 * Removing any active potion effects.
 * Giving them Regeneration II for 20 seconds,
 * Fire resistance for 20 seconds and
 * Absorption I for 5 seconds.
 * Cancelling the damage event.
 *
 * And remove the enhancement.
 */
public class UndyingEnhancement implements
    ActiveEnhancement<EntityDamageEvent> {

    private static final PotionEffect REGENERATION_EFFECT =
        new PotionEffect(PotionEffectType.REGENERATION, 400, 2);
    private static final PotionEffect FIRE_RESISTANCE_EFFECT =
        new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 400, 1);
    private static final PotionEffect ABSORPTION_EFFECT =
        new PotionEffect(PotionEffectType.ABSORPTION, 100, 1);

    private EnhancedItemForge forge;

    /**
     * Create a Undying enhancement
     * 
     * @param forge The current EnhancedItemForge
     */
    public UndyingEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    public boolean run(EntityDamageEvent context) {
        // Totems can't save you if the damage was caused by /kill or the void,
        // so neither can this.
        if (context.getCause() == EntityDamageEvent.DamageCause.KILL ||
            context.getCause() == EntityDamageEvent.DamageCause.VOID)
            return false;

        if (context.getEntity() instanceof LivingEntity entity) {
            if (entity.getHealth() - context.getFinalDamage() > 0)
                return false;

            // May be in offhand or mainhand
            ItemStack shield = entity.getEquipment().getItemInOffHand();
            if (shield.getType() == Material.AIR ||
                !forge.create(shield).hasEnhancement(this)) {
                shield = entity.getEquipment().getItemInMainHand();

                if (shield.getType() == Material.AIR ||
                    !forge.create(shield).hasEnhancement(this))
                    return false;
            }

            entity.clearActivePotionEffects();
            entity.addPotionEffect(REGENERATION_EFFECT);
            entity.addPotionEffect(FIRE_RESISTANCE_EFFECT);
            entity.addPotionEffect(ABSORPTION_EFFECT);

            context.setCancelled(true);

            EnhancedItem enhancedShield = forge.create(shield);

            enhancedShield.removeEnhancement(this);
            enhancedShield.update();

            return true;
        }

        return false;
    }

    public String getName() {
        return "undying";
    }

    public TextComponent getSocketMessage() {
        // "<Undying>" where the text "Undying" is yellow and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Undying", NamedTextColor.YELLOW))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.getItemStack().getType() == Material.SHIELD;
    }

    public Class<EntityDamageEvent> getEventType() {
        return EntityDamageEvent.class;
    }
}
