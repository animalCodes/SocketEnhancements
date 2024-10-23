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
 * Withering enhancement, On attacking another entity, have a chance to apply
 * wither to them.
 */
public class WitheringEnhancement implements
    ActiveEnhancement<EntityDamageByEntityEvent> {
    // Chance for enhancement to activated.
    private static final double CHANCE = 0.3;

    private static final PotionEffect WITHER_EFFECT =
        new PotionEffect(PotionEffectType.WITHER, 160, 1);

    private final EnhancedItemForge forge;

    public WitheringEnhancement(EnhancedItemForge forge) {
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

                defender.addPotionEffect(WITHER_EFFECT);

                return true;
            }
        }
        return false;
    }

    public String getName() {
        return "withering";
    }

    public TextComponent getSocketMessage() {
        // "<Withering>" where the text "Withering" is black and the "< >"s
        // are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE,
             TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
            .append(Component.text("Withering", NamedTextColor.BLACK))
            .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.II;
    }

    public boolean isValidItem(EnhancedItem item) {
        return SWORDS.isTagged(item.getItemStack().getType()) ||
            AXES.isTagged(item.getItemStack().getType());
    }

    public Class<EntityDamageByEntityEvent> getEventType() {
        return EntityDamageByEntityEvent.class;
    }
}
