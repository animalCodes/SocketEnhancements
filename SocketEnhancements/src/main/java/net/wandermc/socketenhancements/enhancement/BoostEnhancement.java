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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Boost enhancement.
 *
 * On use, boosts gliding players as if they had used a firework of duration
 * DURATION, at the cost of applying COST damage to the item. There is a CHANCE
 * chance that the rocket will damage the player.
 */
public class BoostEnhancement implements Enhancement, Listener {
    // How much damage will be applied to the item on use.
    private final int COST;
    // The flight duration the player will be boosted with.
    private final int DURATION;
    // Chance for boost to damage player.
    private final double DAMAGE_CHANCE;
    // The virtual firework rocket used to boost the player.
    private final ItemStack ROCKET;
    // Alternative rocket, damages player when used.
    private final ItemStack DAMAGE_ROCKET;

    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<red>Boost<white>>");

    private final EnhancedItemForge forge;

    /**
     * Create a BoostEnhancement.
     *
     * `config` defaults:
     * - "cost": 8
     * - "duration": 2
     * - "damage_chance": 0.15
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public BoostEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;
        this.COST = config.getInt("cost", 8);
        this.DURATION = config.getInt("duration", 2);
        this.DAMAGE_CHANCE = config.getDouble("damage_chance", 0.15);

        this.ROCKET = Bukkit.getServer().getItemFactory().createItemStack(
            "minecraft:firework_rocket[fireworks={flight_duration:"+DURATION
            +"}]");

        this.DAMAGE_ROCKET = Bukkit.getServer().getItemFactory()
            .createItemStack("minecraft:firework_rocket[fireworks={" +
                "explosions:[{shape:star,colors:[11743532]}],flight_duration:"
                +DURATION+"}]");

    }

    /**
     * Determine whether `context` matches the conditions for a boost.
     *
     * The player must have interacted with the air while gliding and holding an
     * item with this bound to it, additionally, the item must have more than
     * COST*2 durability.
     *
     * @param context The context to check.
     * @return Whether this enhancement's effect should be run.
     */
    private boolean contextMatches(PlayerInteractEvent context) {
        Player player = context.getPlayer();

        if (!(player.isGliding() &&
            context.getAction() == Action.RIGHT_CLICK_AIR))
            return false;

        if (!(context.hasItem() && forge.has(context.getItem(), this)))
            return false;

        if (context.getItem().getItemMeta() instanceof Damageable damageable) {
            if ((context.getItem().getType().getMaxDurability() -
                damageable.getDamage() <= (COST * 2)))
                return false;
        } else
            return false;

        return true;
    }

    @EventHandler
    public void run(PlayerInteractEvent context) {
        Player player = context.getPlayer();
        if (!contextMatches(context))
            return;

        if (roll(DAMAGE_CHANCE)) {
            player.fireworkBoost(DAMAGE_ROCKET);
        } else {
            player.fireworkBoost(ROCKET);
        }

        context.getItem().damage(COST, player);
    }

    public String name() {
        return "boost";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        if (item.has("blink"))
            return false;
        return item.itemStack().getItemMeta() instanceof Damageable
            && item.itemStack().getType() != Material.ELYTRA;
    }
}
