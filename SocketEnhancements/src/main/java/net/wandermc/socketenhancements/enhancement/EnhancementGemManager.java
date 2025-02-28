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
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
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
import org.bukkit.tag.DamageTypeTags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.util.event.BlockableAction;
import net.wandermc.socketenhancements.util.event.ItemEventBlocker;

/**
 * A class for managing the creation and use of Enhancement Gems.
 *
 * Enhancement gems are Enhancements in item form, allowing players to easily
 * remove, collect and re-bind Enhancements.
 */
public class EnhancementGemManager implements Listener {
    public static final TextComponent ENHANCEMENT_GEM_NAME = (TextComponent)
        MiniMessage.miniMessage().deserialize("<!italic><aqua>Enhancement Gem");

    private final JavaPlugin plugin;
    private final EnhancedItemForge forge;
    private final NamespacedKey gemKey;

    private final ItemStack dummyGem;

    private final Material blockType;
    private final Material gemType;
    private final boolean flammable;

    private final ItemEventBlocker eventBlocker;

    /**
     * Create an EnhancementGemManager for `plugin`.
     *
     * `config` defaults:
     * - block: GRINDSTONE
     * - material: END_CRYSTAL
     * - flammable: false
     *
     * @param plugin The plugin this manager is working for.
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options for enhancement gems. See
     *                      above.
     */
    public EnhancementGemManager(JavaPlugin plugin, EnhancedItemForge forge,
        ConfigurationSection config) {
        this.plugin = plugin;
        this.forge = forge;
        this.gemKey = new NamespacedKey(plugin, "is_gem");

        // blockType
        Material bt = Material.getMaterial(config.getString("block",
            "GRINDSTONE"));
        if (bt == null || bt == Material.AIR)
            bt = Material.GRINDSTONE;
        this.blockType = bt;

        // gemType
        Material gt = Material.getMaterial(config.getString(
            "material", "END_CRYSTAL"));
        if (gt == null || gt == Material.AIR)
            gt = Material.END_CRYSTAL;
        this.gemType = gt;

        this.flammable = config.getBoolean("flammable", false);

        this.dummyGem = createGem().itemStack();

        // eventBlocker
        BlockableAction[] javaIsDumb = {};
        this.eventBlocker = new ItemEventBlocker(plugin,
            item -> isEnhancementGem(item),
            BlockableAction.getValidActions
            (gemType).toArray(javaIsDumb));

        registerRecipe();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Determine whether `item` is an enhancement gem.
     *
     * An item is considered an enhancement gem if it has "is_gem" set to true
     * in its PersistentDataContainer.
     *
     * @param item The item to check.
     * @return Whether `item` is an Enhancement Gem.
     */
    public boolean isEnhancementGem(ItemStack item) {
        if (!item.hasItemMeta())
            return false;
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
     * An "Enhancement Gem" is an item of type `this.gemType` with
     * a single socket. The enhancement in that socket is the "type" of the
     * Enhancement Gem.
     *
     * In the event of an unsuccessful binding, an "empty" ItemStack
     * (ItemStack.empty) will be returned instead.
     *
     * @param enhancement The Enhancement the gem represents.
     * @return An Enhancement Gem OR an empty ItemStack.
     */
    public ItemStack createGemOfType(Enhancement enhancement) {
        EnhancedItem enhancedItem = createGem();
        if (enhancedItem.checklessBind(enhancement))
            return enhancedItem.update();
        else
            return ItemStack.empty();
    }

    /**
     * Create a typeless enhancement gem.
     *
     * @return An Enhancement Gem.
     */
    private EnhancedItem createGem() {
        ItemStack item = new ItemStack(gemType);

        ItemMeta meta = item.getItemMeta();
        if (!flammable)
            meta.setDamageResistant(DamageTypeTags.IS_FIRE);
        meta.displayName(ENHANCEMENT_GEM_NAME);
        meta.getPersistentDataContainer()
            .set(gemKey, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);

        EnhancedItem enhancedItem = forge.create(item);
        enhancedItem.addSockets(1);
        enhancedItem.update();

        return enhancedItem;
    }

    /**
     * Create and register the recipe for applying Enhancement Gems to items.
     */
    private void registerRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(
                new NamespacedKey(plugin, "enhancement_gem_addition"),
                // Result is irrelevant as it will be overridden.
                new ItemStack(Material.STONE, 1));

        // If an item has a socket limit, it can be enhanced
        recipe.addIngredient(new RecipeChoice.MaterialChoice(
                forge.enhanceableMaterials().stream().collect(Collectors.
                    toList())));
        recipe.addIngredient(dummyGem.getType());

        plugin.getServer().addRecipe(recipe);
    }

    /**
     * Convert a bound Enhancement to an Enhancement Gem when interacting with
     * the appropriate block.
     *
     * Default 'interaction' is right-clicking while sneaking.
     *
     * @param event The event.
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
        Enhancement enhancement = last(forge.create(item));
        if (enhancement == null)
            return;

        ItemStack gem = createGemOfType(enhancement);
        if (gem.isEmpty())
            return;

        enhancedItem.remove(enhancement);
        enhancedItem.update();

        event.getPlayer().getWorld().dropItemNaturally(
            event.getClickedBlock().getLocation(), gem);
    }

    /**
     * Add Enhancements to an item when combined with Enhancement Gems in a
     * crafting table.
     *
     * @param event The event.
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
                Enhancement enhancement = last(forge.create(item));
                if (enhancement != null)
                    enhancements.add(enhancement);
            } else
                itemToEnhance = forge.create(item.clone());
        }

        if (enhancements.size() < 1 || itemToEnhance == null)
            return;

        // If a binding fails, don't allow the player to take the item.
        for (Enhancement enhancement : enhancements) {
            if (!itemToEnhance.bind(enhancement)) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }

        event.getInventory().setResult(itemToEnhance.update());
    }

    /**
     * The last (most recently bound) Enhancement on `item`.
     *
     * @return The last Enhancement, or null if none are bound.
     */
    private Enhancement last(EnhancedItem item) {
        Enhancement enhancement = null;
        for (int i = 0; i < item.sockets(); i++) {
            if (item.get(i) instanceof EmptySocket)
                break;

            enhancement = item.get(i);
        }
        return enhancement;
    }
}
