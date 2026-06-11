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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.util.cost.CostItemDamage;

/**
 * Grapple enhancement.
 *
 * Allows players to use fishing rods as grappling hooks. Must be held in
 * mainhand.
 */
public class GrappleEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<gold>Grapple<white>>");

    private final CostItemDamage cost;

    private final EnhancedItemForge forge;

    /**
     * Create a GrappleEnhancement.
     *
     * `config` defaults:
     * cost_amount: 2
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public GrappleEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.cost = new CostItemDamage(config.getInt("cost_amount", 2), true);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(PlayerFishEvent context) {
        if (context.getState() != PlayerFishEvent.State.REEL_IN)
            return;

        // Only allow if used in main hand
        ItemStack rod = context.getPlayer().getInventory()
            .getItemInMainHand();

        if (rod.isEmpty() || !forge.has(rod, this))
            return;

        Vector velocity = context.getHook().getLocation()
            .subtract(context.getPlayer().getLocation()).toVector();

        // The following numbers are the result of testing and seem to work
        // quite well.
        // On rare occasions the enhancement will not modify the player's
        // velocity. I am so far unable to figure out why.

        velocity.setY((velocity.getY() * 0.2) + 0.5);
        if (velocity.getY() <= 0)
            velocity.setY(0.5);

        velocity.setX(velocity.getX() / 4);
        velocity.setZ(velocity.getZ() / 4);

        context.getPlayer().setVelocity(velocity);
    }

    public String name() {
        return "grapple";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.II;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.itemStack().getType() == Material.FISHING_ROD;
    }
}
