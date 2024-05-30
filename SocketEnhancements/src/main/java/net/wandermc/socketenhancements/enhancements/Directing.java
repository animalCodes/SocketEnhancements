/*
 *    This file is part of SocketEnhancements: A gear enhancement plugin for PaperMC servers.
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
package net.wandermc.socketenhancements.enhancements;

import org.bukkit.Material;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.enhancement.ActiveEnhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementManager;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Directing enhancement, When a player is struck with lightning, simulate the
 * effect of them eating a (normal) golden apple.
 */
public class Directing implements ActiveEnhancement<EntityDamageByEntityEvent> {
    private static final PotionEffect ABSORPTION_EFFECT =
        new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60 * 2, 1);
    private static final PotionEffect REGENERATION_EFFECT =
        new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 2);
    private static final PotionEffect FIRE_RESISTANCE_EFFECT =
        new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 10, 1);
    
    private EnhancedItemForge forge;
    
    /**
     * Create a Directing enhancement
     * 
     * @param forge The current EnhancedItemForge
     */
    public Directing(EnhancedItemForge forge) {
        this.forge = forge;
    }

    /**
     * Simulate the effect of `player` eating a golden apple.
     *
     * @param player the player to force-feed.
     */
    private void eatGoldenApple(Player player) {
        player.setFoodLevel(player.getFoodLevel() + 4);
        player.setSaturation(player.getSaturation() + 9.6f);
        player.addPotionEffect(ABSORPTION_EFFECT);
        player.addPotionEffect(REGENERATION_EFFECT);
        // Getting struck by lightning sets the player on fire, which will
        // nullify the benefits of "eating a golden apple". So give them fire
        // resistance as well so this enhancement isn't entirely pointless.
        player.addPotionEffect(FIRE_RESISTANCE_EFFECT);
    }

    public boolean run(EntityDamageByEntityEvent context) {
        if (!(context.getDamager() instanceof LightningStrike))
            return false;
        if (context.getEntity() instanceof Player player) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet == null || !forge.create(helmet).hasEnhancement(this))
                return false;

            eatGoldenApple(player);
        } else
            return false;
        return true;
    }

    public String getName() {
        return "directing";
    }

    public TextComponent getSocketMessage() {
        // "<Directing>" where the text "Directing" is aqua and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Directing", NamedTextColor.AQUA))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.getItemStack().getType().toString().contains("HELMET");
    }

    public Class<EntityDamageByEntityEvent> getEventType() {
        return EntityDamageByEntityEvent.class;
    }
}

