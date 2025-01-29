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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.PICKAXES;

import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Explosive enhancement, on mining a block also destroy all neighbouring blocks
 * within a 1-block radius. (3*3 cube centered on mined block)
 */
public class ExplosiveEnhancement implements Enhancement, Listener {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<dark_red>Explosive<white>>");

    private final EnhancedItemForge forge;

    public ExplosiveEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    /**
     * Gets all blocks around `origin`, forming a 3*3 cube around it.
     *
     * @param origin The starting block.
     * @return All blocks within 1 block of origin.
     */
    private Block[] getRelatives(Block origin) {
        // The below algorithm should result in exactly 26 blocks. If it finds
        // too many we'll get an ArrayIndexOutOfBoundsException, if too little
        // we'll get an NPE later on. Either way it'll "fail loudly", which IMO
        // is always better than failing "quietly".
        Block[] blocks = new Block[26];
        int i = 0;
        // Generate every possible combination of x, y and z where each can be
        // -1, 0 or 1. EXCEPT for 0, 0, 0.
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) {
                blocks[i++] = origin.getRelative(x, -1, z);
                // Avoid 0, 0, 0.
                if (!(x == 0 && z == 0))
                    blocks[i++] = origin.getRelative(x, 0, z);
                blocks[i++] = origin.getRelative(x, 1, z);
            }
        return blocks;
    }

    @EventHandler
    public void run(BlockBreakEvent context) {
        ItemStack pickaxe = context.getPlayer().getInventory()
            .getItemInMainHand();

        if (pickaxe.isEmpty() || !forge.create(pickaxe).hasEnhancement(this))
            return;

        context.getPlayer().spawnParticle(
            Particle.EXPLOSION, context.getBlock().getLocation(), 10);
        context.getPlayer().playSound(context.getBlock().getLocation(),
            Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, 1, 1);

        int damage = 0;
        for (Block relative : getRelatives(context.getBlock())) {
            // Only mining blocks with a maximum blast resistance of 10 will
            // let us mine up to endstone but no further, hopefully that's a
            // good balance.
            if (relative.getType().getBlastResistance() <= 10) {
                // Normally, blocks with a hardness of 0 will be mined instantly
                // regardless of the tool used and won't deduct durability from
                // the item. Consistency is nice, so let's copy that behaviour.
                if (relative.getType().getHardness() > 0)
                    damage++;
                relative.breakNaturally(pickaxe);
            }
        }

        pickaxe.damage(damage, context.getPlayer());
    }

    public String getName() {
        return "explosive";
    }

    public TextComponent getSocketMessage() {
        return socketMessage;
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        return PICKAXES.isTagged(item.getItemStack().getType());
    }

    public Class<BlockBreakEvent> getEventType() {
        return BlockBreakEvent.class;
    }
}
