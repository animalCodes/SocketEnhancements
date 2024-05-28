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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.enhancement.ActiveEnhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Boost enhancement, On use, boosts gliding players as if they had used a
 * firework of duration DURATION, at the cost of applying COST damage to the
 * item.
 */
public class Boost implements ActiveEnhancement<PlayerInteractEvent> {
    // How much damage will be applied to the item on use.
    private static final int COST = 8;
    // The flight duration the player will be boosted with.
    private static final int DURATION = 2;
    // The virtual firework rocket used to boost the player.
    private static final ItemStack rocket =
        Bukkit.getServer().getItemFactory().createItemStack(
        "minecraft:firework_rocket{Fireworks:{Flight:"+DURATION+"}}");

    private final EnhancedItemForge forge;

    public Boost(EnhancedItemForge forge) {
        this.forge = forge;
    }

    /**
     * Determines whether `context` matches the conditions for a boost.
     *
     * The player must have interacted with the air while gliding and holding an
     * item with this bound to it, additionally, the item must have more than
     * COST*2 durability OR the player must be in creative mode.
     *
     * @param context The context to check.
     * @return Whether this enhancement's effect should be run.
     */
    private boolean contextMatches(PlayerInteractEvent context) {
        Player player = context.getPlayer();

        if (!(player.isGliding() &&
            context.getAction() == Action.RIGHT_CLICK_AIR))
            return false;

        if (!(context.hasItem() &&
            forge.create(context.getItem()).hasEnhancement(this)))
            return false;

        if (context.getItem().getItemMeta() instanceof Damageable damageable) {
            if ((context.getItem().getType().getMaxDurability() -
                damageable.getDamage() <= (COST * 2))
                && (player.getGameMode() != GameMode.CREATIVE))
                return false;
        } else
            return false;

        return true;
    }

    public boolean run(PlayerInteractEvent context) {
        if (!contextMatches(context))
            return false;

        context.getPlayer().fireworkBoost(rocket);

        Damageable itemDamageMeta = (Damageable)context.getItem().getItemMeta();
        if (context.getPlayer().getGameMode() != GameMode.CREATIVE) {
            itemDamageMeta.setDamage(itemDamageMeta.getDamage()+COST);
        }
        context.getItem().setItemMeta(itemDamageMeta);

        return true;
    }

    public String getName() {
        return "boost";
    }

    public TextComponent getSocketMessage() {
        // "<Boost>" where the text "Boost" is red and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Boost", NamedTextColor.DARK_RED))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        if (item.hasEnhancement("blink"))
            return false;
        return item.update().getItemMeta() instanceof Damageable
            && item.update().getType() != Material.ELYTRA;
    }

    public Class<PlayerInteractEvent> getEventType() {
        return PlayerInteractEvent.class;
    }
}
