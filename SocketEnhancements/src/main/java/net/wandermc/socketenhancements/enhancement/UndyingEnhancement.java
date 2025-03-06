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

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Undying enhancement.
 *
 * If an entity takes fatal damage from any source other than the void or /kill
 * while holding enhanced item, prevent death, apply configurable buffs and
 * remove the enhancement.
 */
public class UndyingEnhancement implements Enhancement, Listener {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<yellow>Undying<white>>");

    private final ArrayList<PotionEffect> potionEffects;

    private EnhancedItemForge forge;

    /**
     * Create an Undying enhancement.
     *
     * `config` defaults:
     * effects:
     *   - effect: minecraft:regeneration
     *     duration: 400
     *     amplifier: 2
     *   - effect: minecraft:fire_resistance
     *     duration: 400
     *     amplifier: 1
     *   - effect: minecraft:absorption
     *     duration: 100
     *     amplifier: 1
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public UndyingEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.potionEffects = new ArrayList();
        config.getMapList("effects").forEach(rawMap -> {
            HashMap convMap = new HashMap();
            rawMap.forEach((k, v) -> convMap.put(k.toString(), v));
            try {
                this.potionEffects.add(new PotionEffect(convMap));
            } catch (Exception e) {}
        });

        if (this.potionEffects.size() == 0) {
            this.potionEffects.add(new PotionEffect(
                PotionEffectType.REGENERATION, 400, 2));
            this.potionEffects.add(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, 400, 1));
            this.potionEffects.add(new PotionEffect(
                PotionEffectType.ABSORPTION, 100, 1));
        }
    }

    /**
     * Apply the "cosmetic" effects of an undying item activating.
     *
     * @param player the Player to apply the effects to.
     */
    private static void applyCosmetics(Player player) {
        player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(),
            10);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 5, 10);
    }

    @EventHandler
    public void run(EntityDamageEvent context) {
        // Totems can't save you if the damage was caused by /kill or the void,
        // so neither can this.
        if (context.getCause() == EntityDamageEvent.DamageCause.KILL ||
            context.getCause() == EntityDamageEvent.DamageCause.VOID)
            return;

        if (context.getEntity() instanceof LivingEntity entity) {
            if (entity.getHealth() - context.getFinalDamage() > 0)
                return;

            // May be in offhand or mainhand
            ItemStack shield = entity.getEquipment().getItemInOffHand();
            if (shield.isEmpty() || !forge.has(shield, this)) {
                shield = entity.getEquipment().getItemInMainHand();

                if (shield.isEmpty() || !forge.has(shield, this))
                    return;
            }

            entity.clearActivePotionEffects();
            potionEffects.forEach(effect -> entity.addPotionEffect(effect));

            if (entity instanceof Player player)
                applyCosmetics(player);

            context.setCancelled(true);

            EnhancedItem enhancedShield = forge.create(shield);

            enhancedShield.remove(this);
            enhancedShield.update();
        }
    }

    public String name() {
        return "undying";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.itemStack().getType() == Material.SHIELD;
    }
}
