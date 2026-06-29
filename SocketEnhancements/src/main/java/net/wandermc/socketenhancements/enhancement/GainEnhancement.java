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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.ARMOR;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Gain enhancement.
 *
 * For each enhanced armour piece, slowly gain experience points.
 * Delay between 'gains', amount of points to gain and chance to actually gain
 * points (both per armour piece) are configurable.
 */
public class GainEnhancement implements PassiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<green>Gain<white>>");

    private long delay;
    private double chance;
    private int points;

    private EnhancedItemForge forge;

    /**
     * Create a Gain enhancement.
     *
     * `config` defaults:
     * delay: 8
     * chance: 0.15
     * points: 4
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public GainEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.delay = config.getLong("delay", 8);
        if (this.delay < 1)
            this.delay = 8;

        // Convert to ticks
        this.delay *= 20;

        this.chance = config.getDouble("chance", 0.15);
        if (this.chance <= 0)
            this.chance = 0.15;

        this.points = config.getInt("points", 4);
        if (this.points < 0)
            this.points = 4;
    }

    public void run() {
        for (Player player : getPlayers()) {
            int gain = 0;
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (item == null || item.isEmpty())
                    continue;

                if (forge.has(item, this) && roll(chance))
                    gain += points;
            }

            if (gain > 0) {
                player.setExperienceLevelAndProgress(
                    player.calculateTotalExperiencePoints() + gain);
            }
        }
    }

    public long period() {
        return delay;
    }

    public String name() {
        return "gain";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return ARMOR.isTagged(item.itemStack().getType());
    }
}
