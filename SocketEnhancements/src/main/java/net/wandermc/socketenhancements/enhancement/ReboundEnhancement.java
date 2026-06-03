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
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import static io.papermc.paper.tag.BaseTag.ITEMS_FOOT_ARMOR;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.util.cost.CostExperiencePoints;

/**
 * Rebound enhancement.
 *
 * Launches player upwards via a wind charge if they take lava or void damage
 * and cancels damage.
 *
 * Costs experience points (configurable)
 */
public class ReboundEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<dark_purple>Rebound<white>>");

    // Applied when the enhancement is activated, primarily to avoid spawning
    // multiple wind charges when, say, the player comes into contact with
    // multiple lava blocks at once.
    private static final PotionEffect slowFallPotion = new PotionEffect(
        PotionEffectType.SLOW_FALLING, 5, 1, false, false, false);

    private final EnhancedItemForge forge;

    private final CostExperiencePoints cost;

    /**
     * Create a ReboundEnhancement.
     *
     * `config` defaults:
     * cost_amount: 8
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public ReboundEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.cost = new CostExperiencePoints(config.getInt("cost_amount", 8));
    }

    @EventHandler(ignoreCancelled=true)
    public void run(EntityDamageEvent context) {
        if (context.getCause() != DamageCause.VOID
            && context.getCause() != DamageCause.LAVA)
            return;

        if (context.getEntity() instanceof Player player) {
            if (!cost.met(player) || player.getPotionEffect(
                PotionEffectType.SLOW_FALLING) != null)
                return;

            ItemStack boots = player.getInventory().getBoots();

            if (boots.isEmpty() || !forge.has(boots, this))
                return;

            // Prevent any damage from being taken
            context.setCancelled(true);
            if (player.getFireTicks() > 0)
                player.setFireTicks(0);

            // Prevent activating the enhancement multiple times in the same
            // instant
            player.addPotionEffect(slowFallPotion);

            // Reset player's downwards velocity
            // Otherwise, if they are falling too fast this will be ineffective
            player.setVelocity(new Vector(player.getVelocity().getX(), 0,
                player.getVelocity().getZ()));

            AbstractWindCharge windCharge = (AbstractWindCharge)
                player.getWorld().spawnEntity(player.getLocation(),
                    EntityType.BREEZE_WIND_CHARGE);

            windCharge.explode();

            cost.take(player);
        }
    }

    public String name() {
        return "rebound";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return ITEMS_FOOT_ARMOR.isTagged(item.itemStack().getType());
    }
}
