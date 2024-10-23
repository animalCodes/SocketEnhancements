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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import static com.destroystokyo.paper.MaterialTags.SWORDS;
import static com.destroystokyo.paper.MaterialTags.AXES;

import net.wandermc.socketenhancements.enhancement.ActiveEnhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Icy enhancement, On attacking another entity, have a chance to
 * - Extinguish them if they are on fire.
 * - Otherwise freeze them and give them mining fatigue.
 */
public class IcyEnhancement implements
    ActiveEnhancement<EntityDamageByEntityEvent> {
    // Chance for enhancement to activated.
    private static final double CHANCE = 0.2;

    private static final PotionEffect MINING_FATIGUE_EFFECT =
        new PotionEffect(PotionEffectType.SLOW_DIGGING, 70, 2);

    private final EnhancedItemForge forge;

    public IcyEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    public boolean run(EntityDamageByEntityEvent context) {
        if (context.getDamager() instanceof LivingEntity attacker) {
            if (context.getEntity() instanceof LivingEntity defender) {
                ItemStack weapon = attacker.getEquipment().getItemInMainHand();
                if (weapon.getType() == Material.AIR ||
                    !forge.create(weapon).hasEnhancement(this))
                    return false;

                if (!roll(CHANCE))
                    return false;

                if (defender.getFireTicks() > 0) {
                    defender.setFireTicks(0);
                    return true;
                }

                defender.setFreezeTicks(defender.getMaxFreezeTicks());
                defender.addPotionEffect(MINING_FATIGUE_EFFECT);

                return true;
            }
        }
        return false;
    }

    public String getName() {
        return "icy";
    }

    public TextComponent getSocketMessage() {
        // "<Icy>" where the text "Icy" is aqua and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE,
             TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
            .append(Component.text("Icy", NamedTextColor.AQUA))
            .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.II;
    }

    public boolean isValidItem(EnhancedItem item) {
        // TODO: don't allow binding if item has fire aspect
        return SWORDS.isTagged(item.getItemStack().getType()) ||
            AXES.isTagged(item.getItemStack().getType());
    }

    public Class<EntityDamageByEntityEvent> getEventType() {
        return EntityDamageByEntityEvent.class;
    }
}
