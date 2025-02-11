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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
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
 * Halves damage taken from flying into walls.
 */
public class CushioningEnhancement implements Enhancement, Listener {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<gray>Cushioning<white>>");

    private EnhancedItemForge forge;

    /**
     * Create a Cushioning enhancement.
     * 
     * @param forge The current EnhancedItemForge.
     */
    public CushioningEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    @EventHandler
    public void run(EntityDamageEvent context) {
        if (context.getCause() != DamageCause.FLY_INTO_WALL)
            return;

        if (context.getEntity() instanceof Player player) {
            ItemStack helmet = player.getInventory().getHelmet();

            if (helmet != null && forge.create(helmet).has(this)) {
                context.setDamage(context.getDamage() / 2);
                player.spawnParticle(Particle.CLOUD, player.getLocation(), 2);
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
