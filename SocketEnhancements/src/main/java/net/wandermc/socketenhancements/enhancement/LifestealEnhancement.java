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
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import static io.papermc.paper.tag.BaseTag.ITEMS_AXES;
import static io.papermc.paper.tag.BaseTag.ITEMS_SWORDS;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Lifesteal enhancement.
 *
 * On attacking another entity, have a chance to gain some of the
 * dealt damage as health.
 */
public class LifestealEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<red>Lifesteal<white>>");

    private double chance;
    private double gain;

    private final EnhancedItemForge forge;

    /**
     * Create a LifestealEnhancement.
     *
     * `config` defaults:
     * chance: 0.5
     * gain: 0.25
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public LifestealEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.chance = config.getDouble("chance", 0.5);
        if (this.chance < 0)
            this.chance = 0.5;

        this.gain = config.getDouble("gain", 0.25);
        if (this.gain < 0)
            this.gain = 0.25;
    }

    @EventHandler(ignoreCancelled=true)
    public void run(EntityDamageByEntityEvent context) {
        if (context.getDamager() instanceof LivingEntity attacker) {
            if (!(context.getEntity() instanceof LivingEntity))
                return;

            ItemStack weapon = attacker.getEquipment().getItemInMainHand();
            if (weapon.isEmpty() || !forge.has(weapon, this))
                return;

            if (!roll(chance))
                return;

            double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH)
                .getValue();
            if (attacker.getHealth() >= maxHealth)
                return;

            double newHealth = attacker.getHealth() +
                (context.getFinalDamage() * gain);
            if (newHealth > maxHealth)
                newHealth = maxHealth;

            attacker.setHealth(newHealth);
        }
    }

    public String name() {
        return "lifesteal";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return ITEMS_SWORDS.isTagged(item.itemStack().getType()) ||
            ITEMS_AXES.isTagged(item.itemStack().getType());
    }
}
