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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.HELMETS;

import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Directing enhancement.
 *
 * When a player is struck with lightning, apply various buffs.
 * Default buffs aim to simulate the effect of eating a golden apple.
 */
public class DirectingEnhancement implements Enhancement, Listener {
    private final int foodGain;
    private final float saturationGain;
    private final ArrayList<PotionEffect> potionEffects;

    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<aqua>Directing<white>>");

    private EnhancedItemForge forge;

    /**
     * Create a Directing enhancement.
     *
     * `config` defaults:
     * food_gain: 4
     * saturation_gain: 9.6
     * effects:
     *   - effect: minecraft:absorption
     *     duration: 2400
     *     amplifier: 1
     *   - effect: minecraft:regeneration
     *     duration: 100
     *     amplifier: 2
     *   - effect: minecraft:fire_resistance
     *     duration: 200
     *     amplifier: 1
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public DirectingEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;
        this.foodGain = config.getInt("food_gain", 4);
        this.saturationGain = (float)config.getDouble("saturation_gain", 9.6);

        this.potionEffects = new ArrayList();

        // "If it works it ain't stupid"
        config.getMapList("effects").forEach(rawMap -> {
            HashMap convMap = new HashMap();
            rawMap.forEach((k, v) -> convMap.put(k.toString(), v));
            try {
                this.potionEffects.add(new PotionEffect(convMap));
            } catch (Exception e) {}
        });

        if (this.potionEffects.size() == 0) {
            this.potionEffects.add(new PotionEffect(
                PotionEffectType.ABSORPTION, 20 * 60 * 2, 1));
            this.potionEffects.add(new PotionEffect(
                PotionEffectType.REGENERATION, 20 * 5, 2));
            this.potionEffects.add(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, 20 * 10, 1));
        }
    }

    @EventHandler
    public void run(EntityDamageByEntityEvent context) {
        if (!(context.getDamager() instanceof LightningStrike))
            return;

        if (context.getEntity() instanceof Player player) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet == null || !forge.has(helmet, this))
                return;

            context.setCancelled(true);
            player.setFoodLevel(player.getFoodLevel() + foodGain);
            player.setSaturation(player.getSaturation() + saturationGain);
            potionEffects.forEach(effect -> player.addPotionEffect(effect));
        }
    }

    public String name() {
        return "directing";
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
