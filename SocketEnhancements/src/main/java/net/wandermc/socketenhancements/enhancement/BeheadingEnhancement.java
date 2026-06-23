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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import static io.papermc.paper.tag.BaseTag.ITEMS_AXES;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Beheading enhancement.
 *
 * On killing another player, have a (configurable) chance to drop their head.
 */
public class BeheadingEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><Beheading>");

    private EnhancedItemForge forge;

    private double chance;

    /**
     * Create a BeheadingEnhancement.
     *
     * `config` defaults:
     * chance: 0.4
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public BeheadingEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.chance = config.getDouble("chance", 0.4);
        if (this.chance < 0)
            this.chance = 0.4;
    }

    @EventHandler(ignoreCancelled=true)
    public void run(PlayerDeathEvent context) {
        Entity entity = context.getDamageSource().getCausingEntity();
        if (entity != null && entity instanceof LivingEntity killer) {
            ItemStack axe = killer.getEquipment().getItemInMainHand();

            if (axe.isEmpty() || !forge.has(axe, this))
                return;

            if (!roll(chance))
                return;

            // Create skull and spawn in world at victim's position
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(context.getPlayer());
            skull.setItemMeta(meta);

            context.getPlayer().getWorld().dropItemNaturally(
                context.getPlayer().getLocation(), skull);
        }
    }

    public String name() {
        return "beheading";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        return ITEMS_AXES.isTagged(item.itemStack().getType());
    }
}
