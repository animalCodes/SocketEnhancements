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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.ARMOR;

import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Scorching enhancement.
 *
 * When wearer is attacked, has a chance to give defender fire resistance, knock
 * back attacker and set them on fire for a brief period, chance increases with
 * each enhanced armour piece.
 */
public class ScorchingEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<yellow>Scorching<white>>");

    private final double chancePerItem;
    private final int fireTicks;
    private final double knockbackStrength;
    private final PotionEffect effect;

    private EnhancedItemForge forge;

    /**
     * Create a Scorching enhancement.
     *
     * `config` defaults:
     * "chance_per": 0.2
     * "fire_ticks": 40
     * "knockback": 0.5
     *
     * "knockback" must be > 0.
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public ScorchingEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.chancePerItem = config.getDouble("chance_per", 0.15);
        this.fireTicks = config.getInt("fire_ticks", 30);

        double knockback = config.getDouble("knockback", 0.5);
        if (knockback <= 0)
            knockback = 0.5;
        this.knockbackStrength = knockback;

        this.effect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
            (int)(this.fireTicks * 1.5), 1);
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
                    defender.addPotionEffect(effect);
                    attacker.setFireTicks(attacker.getFireTicks()+fireTicks);
                    attacker.knockback(knockbackStrength,
                        defender.getX() - attacker.getX(),
                        defender.getZ() - attacker.getZ());
                }
            }
        }
    }

    public String name() {
        return "scorching";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        if (item.has("frigid"))
            return false;
        return ARMOR.isTagged(item.itemStack().getType());
    }
}
