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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
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
 * Explosive enhancement, on mining a block also destroy all neighbouring blocks
 * within a 1-block radius. (3*3 cube centered on mined block)
 */
public class Explosive implements ActiveEnhancement<BlockBreakEvent> {
    private final EnhancedItemForge forge;

    public Explosive(EnhancedItemForge forge) {
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

    public boolean run(BlockBreakEvent context) {
        ItemStack pickaxe = context.getPlayer().getInventory()
            .getItemInMainHand();

        if (pickaxe.getType() == Material.AIR ||
            !forge.create(pickaxe).hasEnhancement(this))
            return false;

        context.getPlayer().spawnParticle(
            Particle.EXPLOSION_LARGE, context.getBlock().getLocation(), 10);
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

        if (pickaxe.getItemMeta() instanceof Damageable meta) {
            // Bypasses unbreaking, it is an explosion after all.
            meta.setDamage(meta.getDamage()+damage);
            pickaxe.setItemMeta(meta);
        }

        return true;
    }

    public String getName() {
        return "explosive";
    }

    public TextComponent getSocketMessage() {
        // "<Explosive>" where the text "Explosive" is dark red and the "< >"s
        // are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Explosive", NamedTextColor.DARK_RED))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.I;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.getItemStack().getType().toString().contains("PICKAXE");
    }

    public Class<BlockBreakEvent> getEventType() {
        return BlockBreakEvent.class;
    }
}
