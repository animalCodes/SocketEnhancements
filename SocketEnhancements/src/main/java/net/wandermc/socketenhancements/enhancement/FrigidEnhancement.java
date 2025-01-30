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

import static com.destroystokyo.paper.MaterialTags.ARMOR;

import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Frigid enhancement.
 *
 * When worn, has a chance to freeze and apply mining fatigue to attackers.
 * Chance increases per enhanced armour piece.
 */
public class FrigidEnhancement implements Enhancement, Listener {
    // Chance for effect to be applied per armour piece.
    private static final double CHANCE_PER = 0.15;

    private static final PotionEffect MINING_FATIGUE_EFFECT =
        new PotionEffect(PotionEffectType.MINING_FATIGUE, 70, 2);

    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<aqua>Frigid<white>>");

    private EnhancedItemForge forge;

    /**
     * Create a Frigid enhancement.
     *
     * @param forge The current EnhancedItemForge.
     */
    public FrigidEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    @EventHandler
    public void run(EntityDamageByEntityEvent context) {
        if (context.getEntity() instanceof LivingEntity defender) {
            if (context.getDamager() instanceof LivingEntity attacker) {
                double chance = 0;
                for (ItemStack armourPiece : defender.getEquipment()
                    .getArmorContents()) {
                    if (armourPiece == null ||
                        armourPiece.isEmpty())
                        continue;

                    if (forge.create(armourPiece).hasEnhancement(this))
                        chance += CHANCE_PER;
                }

                if (roll(chance)) {
                    attacker.setFreezeTicks(attacker.getMaxFreezeTicks());
                    attacker.addPotionEffect(MINING_FATIGUE_EFFECT);
                }
            }
        }
    }

    public String getName() {
        return "frigid";
    }

    public TextComponent getSocketMessage() {
        return socketMessage;
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
