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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.enhancement.ActiveEnhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Lifesteal enhancement, On attacking another entity, have a CHANCE chance to
 * gain a quarter of the dealt damage as health.
 */
public class Lifesteal implements ActiveEnhancement<EntityDamageByEntityEvent> {
    private static final double CHANCE = 0.5;

    private final EnhancedItemForge forge;

    public Lifesteal(EnhancedItemForge forge) {
        this.forge = forge;
    }

    public boolean run(EntityDamageByEntityEvent context) {
        if (context.getDamager() instanceof LivingEntity attacker) {
            if (!(context.getEntity() instanceof LivingEntity))
                return false;
            ItemStack weapon = attacker.getEquipment().getItemInMainHand();
            if (weapon.getType() == Material.AIR ||
                !forge.create(weapon).hasEnhancement(this))
                return false;

            if (!roll(CHANCE))
                return false;

            double maxHealth = attacker.getAttribute(
                Attribute.GENERIC_MAX_HEALTH).getValue();
            if (attacker.getHealth() >= maxHealth)
                return false;

            double newHealth = attacker.getHealth() +
                (context.getFinalDamage() / 4);
            if (newHealth > maxHealth)
                newHealth = maxHealth;

            attacker.setHealth(newHealth);
            return true;
        }
        return false;
    }

    public String getName() {
        return "lifesteal";
    }

    public TextComponent getSocketMessage() {
        // "<Lifesteal>" where the text "Lifesteal" is red and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Lifesteal", NamedTextColor.DARK_RED))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        String type = item.getItemStack().getType().toString();
        return type.contains("_AXE") || type.contains("SWORD");
    }

    public Class<EntityDamageByEntityEvent> getEventType() {
        return EntityDamageByEntityEvent.class;
    }
}
