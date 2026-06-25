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

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Soulbound Enhancement.
 *
 * Items with this enhancement will remain in a player's inventory on death. May
 * be consumed on use.
 *
 * NOT enabled by default.
 */
public class SoulboundEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<aqua>Soulbound<white>>");

    private final EnhancedItemForge forge;

    private final boolean singleUse;

    /**
     * Create a Soulbound enhancement.
     *
     * `config` defaults:
     * single_use: false
     *
     * @param forge The current EnhancedItemForge
     * @param config Configuration options.
     */
    public SoulboundEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.singleUse = config.getBoolean("single_use", false);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(PlayerDeathEvent context) {
        if (context.getKeepInventory())
            return;

        List<ItemStack> keep = context.getItemsToKeep();
        List<ItemStack> drops = context.getDrops();

        for (ItemStack drop : drops) {
            EnhancedItem item = forge.create(drop);

            if (item.has(this)) {
                if (singleUse) {
                    item.remove(this);
                    keep.add(item.update());
                } else {
                    keep.add(drop);
                }
            }
        }

        drops.remove(keep);
    }

    public String name() {
        return "soulbound";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return true;
    }
}
