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
 * Withering enhancement.
 *
 * On attacking another entity, have a chance to apply wither to them.
 */
public class WitheringEnhancement implements Enhancement, Listener {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<dark_gray>Withering<white>>");

    private final double chance;
    private final PotionEffect effect;

    private final EnhancedItemForge forge;

    /**
     * Create a WitheringEnhancement.
     *
     * `config` defaults:
     * "chance": 0.3
     * "duration": 160
     * "amplifier": 1
     *
     * @param forge The current EnhancedItemForge.
     * @param config ConfigurationOptions.
     */
    public WitheringEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;
        this.chance = config.getDouble("chance", 0.3);

        int duration = config.getInt("duration", 160);
        if (duration <= 0)
            duration = 160;

        int amplifier = config.getInt("amplifier", 1);
        if (amplifier <= 0)
            amplifier = 1;

        this.effect = new PotionEffect(PotionEffectType.WITHER, duration,
            amplifier);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(EntityDamageByEntityEvent context) {
        if (context.getDamager() instanceof LivingEntity attacker) {
            if (context.getEntity() instanceof LivingEntity defender) {
                ItemStack weapon = attacker.getEquipment().getItemInMainHand();
                if (weapon.isEmpty() || !forge.has(weapon, this))
                    return;

                if (!roll(chance))
                    return;

                defender.addPotionEffect(effect);
            }
        }
    }

    public String name() {
        return "withering";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.II;
    }

    public boolean isValidItem(EnhancedItem item) {
        return SWORDS.isTagged(item.itemStack().getType()) ||
            AXES.isTagged(item.itemStack().getType());
    }
}
