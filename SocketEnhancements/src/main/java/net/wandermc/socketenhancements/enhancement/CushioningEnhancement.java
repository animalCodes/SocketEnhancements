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
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.HELMETS;

import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Cushioning enhancement.
 *
 * Reduces damage taken from flying into walls.
 */
public class CushioningEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<gray>Cushioning<white>>");

    private final EnhancedItemForge forge;

    private final double damageTaken;

    /**
     * Create a Cushioning enhancement.
     *
     * `config` defaults:
     * damage_taken: 0.5
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration Options.
     */
    public CushioningEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;
        this.damageTaken = config.getDouble("damage_taken", 0.5);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(EntityDamageEvent context) {
        if (context.getCause() != DamageCause.FLY_INTO_WALL)
            return;

        if (context.getEntity() instanceof Player player) {
            ItemStack helmet = player.getInventory().getHelmet();

            if (helmet != null && forge.has(helmet, this)) {
                context.setDamage(context.getDamage() * damageTaken);
                player.getWorld().spawnParticle(Particle.CLOUD,
                    player.getLocation(), 2);
            }
        }
    }

    public String name() {
        return "cushioning";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        return HELMETS.isTagged(item.itemStack().getType());
    }
}
