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

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.PICKAXES;

import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Capturing enhancement
 *
 * Allows spawners to be mined with pickaxes.
 * May be consumed in the process (configurable)
 */
public class CapturingEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<blue>Capturing<white>>");

    private final EnhancedItemForge forge;

    private final boolean singleUse;

    /**
     * Create a CapturingEnhancement.
     *
     * `config` defaults:
     * single_use: false
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public CapturingEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.singleUse = config.getBoolean("single_use", false);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(BlockBreakEvent context) {
        ItemStack pickaxe = context.getPlayer().getInventory()
            .getItemInMainHand();

        if (pickaxe.isEmpty() || !forge.has(pickaxe, this))
            return;

        if (context.getBlock().getType() != Material.SPAWNER)
            return;

        Block spawner = context.getBlock();
        spawner.getWorld().dropItem(spawner.getLocation(),
            spawnerBlockToItem(spawner));

        if (singleUse) {
            EnhancedItem enhancedPickaxe = forge.create(pickaxe);
            enhancedPickaxe.remove(this);
            enhancedPickaxe.update();
        }

        context.getPlayer().playSound(spawner.getLocation(),
            Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.NEUTRAL, 5, 10);
    }

    /**
    * Allow players in survival mode to place spawners.
    */
    @EventHandler(ignoreCancelled=true)
    public void placeSpawners(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.SPAWNER || event.getPlayer()
            .getGameMode() != GameMode.SURVIVAL)
            return;

        CreatureSpawner itemBlockState = (CreatureSpawner)
            ((BlockStateMeta)event.getItemInHand().getItemMeta())
            .getBlockState();

        CreatureSpawner blockState = (CreatureSpawner)block.getState();

        blockState.setDelay(itemBlockState.getDelay());
        blockState.setMaxNearbyEntities(itemBlockState
            .getMaxNearbyEntities());
        blockState.setMaxSpawnDelay(itemBlockState.getMaxSpawnDelay());
        blockState.setMinSpawnDelay(itemBlockState.getMinSpawnDelay());
        blockState.setRequiredPlayerRange(itemBlockState
            .getRequiredPlayerRange());
        blockState.setSpawnCount(itemBlockState.getSpawnCount());
        blockState.setSpawnedType(itemBlockState.getSpawnedType());
        blockState.setSpawnRange(itemBlockState.getSpawnRange());

        blockState.update();

        event.getPlayer().playSound(block.getLocation(),
            Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.NEUTRAL, 5, 10);
    }

    /**
     * Convert a spawner Block into an ItemStack.
     *
     * @param spawner The spawner block
     * @return The spawner as an ItemStack.
     */
    private ItemStack spawnerBlockToItem(Block spawner) {
        ItemStack item = new ItemStack(Material.SPAWNER);

        BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
        meta.setBlockState((CreatureSpawner)spawner.getState());

        item.setItemMeta(meta);
        return item;
    }

    public String name() {
        return "capturing";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.II;
    }

    public boolean isValidItem(EnhancedItem item) {
        return !item.has("explosive") && PICKAXES.isTagged(item.itemStack()
            .getType());
    }
}
