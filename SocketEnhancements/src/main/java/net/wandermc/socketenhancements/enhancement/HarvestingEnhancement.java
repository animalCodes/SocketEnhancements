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
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static io.papermc.paper.tag.BaseTag.ITEMS_HOES;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.util.cost.CostExperiencePoints;

/**
 * Harvesting enhancement.
 *
 * On interacting with a crop, harvest and replant said crop.
 * May cost experience points.
 */
public class HarvestingEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<dark_green>Harvesting<white>>");

    private final EnhancedItemForge forge;

    private final CostExperiencePoints cost;

    /**
     * Create a HarvestingEnhancement.
     *
     * `config` defaults:
     * cost_amount: 1
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public HarvestingEnhancement(EnhancedItemForge forge,
        ConfigurationSection config) {
        this.forge = forge;

        this.cost = new CostExperiencePoints(config.getInt("cost_amount", 1));
    }

    @EventHandler(ignoreCancelled=false)
    public void run(PlayerInteractEvent context) {
        ItemStack hoe = context.getPlayer().getInventory()
            .getItemInMainHand();

        if (hoe.isEmpty() || !forge.has(hoe, this))
            return;

        Player player = context.getPlayer();
        if (!cost.met(player) ||
            context.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block crop = context.getClickedBlock();
        if (crop.getType() != Material.CHORUS_FLOWER
            && crop.getType() != Material.PUMPKIN_STEM
            && crop.getType() != Material.MELON_STEM
            && crop.getBlockData() instanceof Ageable data) {

            // Only continue if crop is fully grown
            if (data.getAge() < data.getMaximumAge())
                return;

            crop.breakNaturally(hoe);
            hoe.damage(1, player);

            // We're essentially giving the player an extra crop here.. But
            // trying to take one seed item from the drops would be a pain.
            data.setAge(0);
            crop.setBlockData(data);

            cost.take(player);
        }
    }

    public String name() {
        return "harvesting";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        return ITEMS_HOES.isTagged(item.itemStack().getType());
    }
}
