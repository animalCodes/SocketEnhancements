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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import static com.destroystokyo.paper.MaterialTags.ARMOR;

import net.wandermc.socketenhancements.enhancement.ActiveEnhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Frigid enhancement, has a chance to freeze and apply mining fatigue to
 * attackers.
 */
public class FrigidEnhancement implements
    ActiveEnhancement<EntityDamageByEntityEvent> {
    // Chance for effect to be applied per armour piece.
    private static final double CHANCE_PER = 0.15;

    private static final PotionEffect MINING_FATIGUE_EFFECT =
        new PotionEffect(PotionEffectType.SLOW_DIGGING, 70, 2);

    private EnhancedItemForge forge;

    /**
     * Create a Frigid enhancement
     *
     * @param forge The current EnhancedItemForge
     */
    public FrigidEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    public boolean run(EntityDamageByEntityEvent context) {
        if (context.getEntity() instanceof LivingEntity defender) {
            if (context.getDamager() instanceof LivingEntity attacker) {
                double chance = 0;
                for (ItemStack armourPiece : defender.getEquipment()
                    .getArmorContents()) {
                    if (armourPiece == null ||
                        armourPiece.getType() == Material.AIR)
                        continue;

                    if (forge.create(armourPiece).hasEnhancement(this))
                        chance += CHANCE_PER;
                }

                if (roll(chance)) {
                    attacker.setFreezeTicks(attacker.getMaxFreezeTicks());
                    attacker.addPotionEffect(MINING_FATIGUE_EFFECT);
                    return true;
                }
            }
        }
        return false;
    }

    public String getName() {
        return "frigid";
    }

    public TextComponent getSocketMessage() {
        // "<Frigid>" where the text "Frigid" is aqua and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Frigid", NamedTextColor.AQUA))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        if (item.hasEnhancement("scorching"))
            return false;
        return ARMOR.isTagged(item.getItemStack().getType());
    }

    public Class<EntityDamageByEntityEvent> getEventType() {
        return EntityDamageByEntityEvent.class;
    }
}
