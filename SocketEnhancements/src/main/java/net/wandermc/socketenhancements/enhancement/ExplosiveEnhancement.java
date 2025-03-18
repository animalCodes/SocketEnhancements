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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.PICKAXES;

import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Explosive enhancement
 *
 * On mining a block also destroy all neighbouring blocks within a 1-block
 * radius. (3*3 cube centered on mined block)
 * May cost set amount of items which must be held in offhand.
 */
public class ExplosiveEnhancement implements Enhancement, Listener {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<dark_red>Explosive<white>>");

    private final Material costType;
    private final int costAmount;

    private final PluginManager pluginManager = Bukkit.getPluginManager();
    private final EnhancedItemForge forge;

    /**
     * Create an ExplosiveEnhancement.
     *
     * `config` defaults:
     * cost_type: "GUNPOWDER"
     * cost_amount: 2
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public ExplosiveEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        Material costType = Material.getMaterial(config.getString("cost_type",
            "GUNPOWDER"));
        if (costType == Material.AIR || costType == null)
            costType = Material.GUNPOWDER;
        this.costType = costType;

        this.costAmount = config.getInt("cost_amount", 2);
    }

    /**
     * Get all blocks around `origin`, forming a 3*3 cube around it.
     *
     * @param origin The starting block.
     * @return All blocks within 1 block of origin.
     */
    private Block[] getRelatives(Block origin) {
        return new Block[] {
            origin.getRelative(-1, -1, -1),
            origin.getRelative(-1, -1, 0),
            origin.getRelative(-1, -1, 1),
            origin.getRelative(-1, 0, -1),
            origin.getRelative(-1, 0, 0),
            origin.getRelative(-1, 0, 1),
            origin.getRelative(-1, 1, -1),
            origin.getRelative(-1, 1, 0),
            origin.getRelative(-1, 1, 1),
            origin.getRelative(0, -1, -1),
            origin.getRelative(0, -1, 0),
            origin.getRelative(0, -1, 1),
            origin.getRelative(0, 0, -1),
            origin.getRelative(0, 0, 1),
            origin.getRelative(0, 1, -1),
            origin.getRelative(0, 1, 0),
            origin.getRelative(0, 1, 1),
            origin.getRelative(1, -1, -1),
            origin.getRelative(1, -1, 0),
            origin.getRelative(1, -1, 1),
            origin.getRelative(1, 0, -1),
            origin.getRelative(1, 0, 0),
            origin.getRelative(1, 0, 1),
            origin.getRelative(1, 1, -1),
            origin.getRelative(1, 1, 0),
            origin.getRelative(1, 1, 1)
        };
    }

    @EventHandler(ignoreCancelled=true)
    public void run(BlockBreakEvent context) {
        Player player = context.getPlayer();
        ItemStack pickaxe = player.getInventory().getItemInMainHand();

        if (pickaxe.isEmpty() || !forge.has(pickaxe, this))
            return;

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (costAmount > 0 &&
            offhand.getType() != costType || offhand.getAmount() < costAmount)
            return;

        player.spawnParticle(Particle.EXPLOSION, context.getBlock()
            .getLocation(), 10);
        player.playSound(context.getBlock().getLocation(),
            Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, 1, 1);

        EnhancedItem enhancedPickaxe = forge.create(pickaxe);
        // Temporarily remove enhancement to avoid 'cascade' where potentially
        // infinite blocks are broken
        enhancedPickaxe.remove(this);
        enhancedPickaxe.update();

        int damage = 0;
        for (Block relative : getRelatives(context.getBlock())) {
            if (relative.getType().getBlastResistance() <= 10) {
                BlockBreakEvent event = new BlockBreakEvent(relative, player);
                pluginManager.callEvent(event);
                if (event.isCancelled())
                    continue;

                if (relative.getType().getHardness() > 0)
                    damage++;
                relative.breakNaturally(pickaxe);
            }
        }

        enhancedPickaxe.bind(this);
        enhancedPickaxe.update();

        pickaxe.damage(damage, player);

        if (costAmount > 0)
            offhand.setAmount(offhand.getAmount() - costAmount);
    }

    public String name() {
        return "explosive";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        return !item.has("capturing") && PICKAXES.isTagged(item.itemStack()
            .getType());
    }
}
