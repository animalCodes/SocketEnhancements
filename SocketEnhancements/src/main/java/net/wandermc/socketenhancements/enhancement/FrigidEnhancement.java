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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<aqua>Frigid<white>>");

    private final double chancePerItem;
    private final PotionEffect effect;

    private EnhancedItemForge forge;

    /**
     * Create a Frigid enhancement.
     *
     * `config` defaults:
     * chance_per: 0.15
     * duration: 70
     * amplifier: 2
     *
     * @param forge The current EnhancedItemForge.
     */
    public FrigidEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;
        this.chancePerItem = config.getDouble("chance_per", 0.1);

        int duration = config.getInt("duration", 50);
        if (duration <= 0)
            duration = 50;

        int amplifier = config.getInt("amplifier", 1);
        if (amplifier <= 0)
            amplifier = 1;

        this.effect = new PotionEffect(PotionEffectType.MINING_FATIGUE,
            duration, amplifier);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(EntityDamageByEntityEvent context) {
        if (context.getEntity() instanceof LivingEntity defender) {
            if (context.getDamager() instanceof LivingEntity attacker) {
                double chance = 0;
                for (ItemStack armourPiece : defender.getEquipment()
                    .getArmorContents()) {
                    if (armourPiece == null || armourPiece.isEmpty())
                        continue;

                    if (forge.has(armourPiece, this))
                        chance += chancePerItem;
                }

                if (roll(chance)) {
                    if (attacker.getFireTicks() > 0) {
                        attacker.setFireTicks(0);
                    } else {
                        attacker.setFreezeTicks(attacker.getMaxFreezeTicks());
                        attacker.addPotionEffect(effect);
                    }
                }
            }
        }
    }

    public String name() {
        return "frigid";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        if (item.has("scorching"))
            return false;
        return ARMOR.isTagged(item.itemStack().getType());
    }
}
