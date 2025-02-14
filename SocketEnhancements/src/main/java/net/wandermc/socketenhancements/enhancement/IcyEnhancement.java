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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.SWORDS;
import static com.destroystokyo.paper.MaterialTags.AXES;

import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Icy enhancement.
 *
 * On attacking another entity, have a chance to extinguish them if they are on
 * fire. Or otherwise freeze them and give them mining fatigue.
 */
public class IcyEnhancement implements Enhancement, Listener {
    // Chance for enhancement to activated.
    private static final double CHANCE = 0.2;

    private static final PotionEffect MINING_FATIGUE_EFFECT =
        new PotionEffect(PotionEffectType.MINING_FATIGUE, 70, 2);

    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<aqua>Icy<white>>");

    private final EnhancedItemForge forge;

    public IcyEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    @EventHandler
    public void run(EntityDamageByEntityEvent context) {
        if (context.getDamager() instanceof LivingEntity attacker) {
            if (context.getEntity() instanceof LivingEntity defender) {
                ItemStack weapon = attacker.getEquipment().getItemInMainHand();

                if (weapon.isEmpty() || !forge.has(weapon, this))
                    return;

                if (!roll(CHANCE))
                    return;

                if (defender.getFireTicks() > 0) {
                    defender.setFireTicks(0);
                    return;
                }

                defender.setFreezeTicks(defender.getMaxFreezeTicks());
                defender.addPotionEffect(MINING_FATIGUE_EFFECT);
            }
        }
    }

    public String name() {
        return "icy";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.II;
    }

    public boolean isValidItem(EnhancedItem item) {
        // TODO: don't allow binding if item has fire aspect
        return SWORDS.isTagged(item.itemStack().getType()) ||
            AXES.isTagged(item.itemStack().getType());
    }
}
