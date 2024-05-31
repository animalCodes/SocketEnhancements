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
package net.wandermc.socketenhancements.enhancement;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.events.BlockableAction;
import net.wandermc.socketenhancements.events.ItemEventBlocker;

/**
 * Manages the creation and use of Enhancement Gems.
 *
 * Enhancement gems are Enhancements in item form, allowing players to easily
 * remove, collect and re-bind Enhancements.
 */
public class EnhancementGemManager implements Listener {
    /**
     * The name of Enhancement Gems.
     */
    public static final TextComponent ENHANCEMENT_GEM_NAME =
        Component.text("Enhancement Gem",
        Style.style(TextDecoration.ITALIC.withState
            (TextDecoration.State.FALSE)));

    private final JavaPlugin plugin;
    private final EnhancedItemForge forge;
    private final NamespacedKey gemKey;
    private final ItemEventBlocker eventBlocker;

    private final Material blockType;
    private final Material enhancementGemType;
    private final ItemStack dummyGem;

    /**
     * Create an EnhancementGemManager for `plugin`.
     *
     * @param plugin The plugin this manager is working for
     * @param forge The current EnhancedItemForge
     */
    public EnhancementGemManager(JavaPlugin plugin, EnhancedItemForge forge,
        Material blockType, Material enhancementGemType) {
        this.plugin = plugin;
        this.forge = forge;

        this.gemKey = new NamespacedKey(plugin, "is_gem");

        this.blockType = blockType;
        this.enhancementGemType = enhancementGemType;

        BlockableAction[] javaIsDumb = {};
        this.eventBlocker = new ItemEventBlocker(plugin,
            item -> {
                if (item == null)
                    return false;
                else
                    return isEnhancementGem(item);},
            BlockableAction.getValidActions
            (enhancementGemType).toArray(javaIsDumb));

        this.dummyGem = createGem();

        registerRecipe();

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Determine whether `item` is an enhancement gem.
     *
     * @param item The item to check.
     * @return Whether `item` has "is_gem" set to true in its
               PersistentDataContainer.
     */
    public boolean isEnhancementGem(ItemStack item) {
        PersistentDataContainer dataContainer = item.getItemMeta()
        .getPersistentDataContainer();
        if (dataContainer.has(gemKey))
            return dataContainer.get(gemKey, PersistentDataType.BOOLEAN);
        else
            return false;
    }

    /**
     * Create an enhancement gem of type `enhancement`.
     *
     * An "Enhancement Gem" is an item of type `this.enhancementGemType` with
     * a single socket. The enhancement in that socket is the "type" of the
     * Enhancement Gem.
     *
     * @param enhancement The Enhancement the gem represents.
     * @return An Enhancement Gem.
     */
    public ItemStack createGemOfType(Enhancement enhancement) {
        EnhancedItem enhancedItem = forge.create(createGem());
        enhancedItem.bind(enhancement);
        return enhancedItem.update();
    }

    /**
     * Create a typeless enhancement gem.
     *
     * @return An Enhancement Gem.
     */
    private ItemStack createGem() {
        ItemStack item = new ItemStack(enhancementGemType);

        ItemMeta meta = item.getItemMeta();
        meta.displayName(ENHANCEMENT_GEM_NAME);
        meta.getPersistentDataContainer()
            .set(gemKey, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        EnhancedItem enhancedItem = forge.create(item);
        enhancedItem.addSockets(1);

        return enhancedItem.update();
    }

    /**
     * Creates and registers recipe for applying Enhancement Gems to items.
     */
    private void registerRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(
                new NamespacedKey(plugin, "enhancement_gem_addition"),
                new ItemStack(Material.STONE, 1));

        // If an item has a socket limit, it can be enhanced
        recipe.addIngredient(new RecipeChoice.MaterialChoice(
                forge.getEnhanceableMaterials().stream().collect(Collectors.
                    toList())));
        recipe.addIngredient(dummyGem.getType());

        Bukkit.addRecipe(recipe);
    }

    /**
     * Convert a bound Enhancement to an Enhancement Gem when interacting with
     * the appropriate block.
     * 
     * Default 'interaction' is right-clicking while sneaking.
     *
     * @param event The event
     */
    @EventHandler(ignoreCancelled=true)
    public void handleInteract(PlayerInteractEvent event) {
        // TODO make interaction configurable
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock().getType() != blockType)
            return;
        if (!event.getPlayer().isSneaking())
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        ItemStack item = event.getItem();
        // Don't let players remove enhancements from enhancement gems,
        // otherwise infinite duplication glitch!
        if (item == null || isEnhancementGem(item))
            return;

        EnhancedItem enhancedItem = forge.create(item);
        Enhancement enhancement = enhancedItem.pop();
        if (enhancement instanceof EmptySocket)
            return;

        enhancedItem.update();

        event.getPlayer().getWorld().dropItemNaturally(
            event.getClickedBlock().getLocation(),
            createGemOfType(enhancement)
        );
    }

    /**
     * Add Enhancements to an item when combined with Enhancement Gems in a crafting table.
     *
     * @param event The event
     */
    @EventHandler(ignoreCancelled=true)
    public void handleCraft(PrepareItemCraftEvent event) {
        // Find gems and item to be enhanced in crafting "matrix"
        ArrayList<Enhancement> enhancements = new ArrayList<>();
        EnhancedItem itemToEnhance = null;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null) // Empty slots are represented by null
                continue;

            if (isEnhancementGem(item)) {
                Enhancement enhancement = forge.create(item).pop();
                enhancements.add(enhancement);
            } else
                itemToEnhance = forge.create(item.clone());
        }

        // Maybe we shouldn't add an empty list to a none-existent item, idk.
        if (enhancements.size() < 1 || itemToEnhance == null)
            return;

        // Try to add all enhancements
        // if a binding fails, don't allow the player to take the item.
        for (Enhancement enhancement : enhancements) {
            if (!itemToEnhance.bind(enhancement)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }

        event.getInventory().setResult(itemToEnhance.update());
    }
}
