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
package net.wandermc.socketenhancements.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
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
 * Manages the crafting and usage of orbs of binding.
 *
 * The only requirement to enable orbs of binding is to construct one of these.
 */
public class OrbOfBindingManager implements Listener {
    private static final TextComponent ORB_OF_BINDING_NAME = (TextComponent)
        MiniMessage.miniMessage().deserialize("<!italic>Orb Of Binding");

    private final JavaPlugin plugin;
    private final EnhancedItemForge forge;
    private final ItemEventBlocker eventBlocker;

    private final ItemStack orbOfBinding;

    private final Material orbOfBindingType;
    private final List<Material> ingredients;
    private final boolean flammable;

    /**
     * Create an OrbOfBindingManager for `plugin`.
     *
     * `config` defaults:
     * - material: CONDUIT
     * - ingredients: [GHAST_TEAR, PRISMARINE_SHARD, CHORUS_FRUIT]
     * - flammable: false
     *
     * All strings must be valid Materials but not AIR.
     * "ingredients" must have at least one material. Only the first 9 will
     * be used.
     *
     * @param plugin The plugin this manager is working for.
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options for orbs of binding.
     */
    public OrbOfBindingManager(JavaPlugin plugin, EnhancedItemForge forge,
        ConfigurationSection config) {
        this.plugin = plugin;
        this.forge = forge;

        Material orbType = Material.getMaterial(config.getString("material",
            "AIR"));
        if (orbType == null || orbType == Material.AIR)
            orbType = Material.CONDUIT;
        this.orbOfBindingType = orbType;

        this.ingredients = retrieveIngredients(config);

        this.flammable = config.getBoolean("flammable", false);

        this.orbOfBinding = createOrbOfBinding();

        BlockableAction[] a = {};
        this.eventBlocker = new ItemEventBlocker(plugin,
            item -> item.isSimilar(this.orbOfBinding),
            BlockableAction.getValidActions(orbOfBindingType).toArray(a));

        registerRecipes();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Retrieve the list of ingredients to craft an orb of binding.
     *
     * @param config The orbs of binding configuration.
     * @return Orb ingredients.
     */
    private List<Material> retrieveIngredients(ConfigurationSection config) {
        ArrayList<Material> ingredients = new ArrayList<Material>();

        for (String ingredient : config.getStringList("ingredients")) {
            Material material = Material.getMaterial(ingredient);
            if (material != null && material != Material.AIR)
                ingredients.add(material);

            if (ingredients.size() >= 9)
                break;
        }

        if (ingredients.size() == 0) {
            ingredients.add(Material.GHAST_TEAR);
            ingredients.add(Material.PRISMARINE_SHARD);
            ingredients.add(Material.CHORUS_FRUIT);
        }

        ingredients.trimToSize();
        return ingredients;
    }

    /**
     * Create an Orb of Binding.
     *
     * @return An Orb of Binding ItemStack.
     */
    private ItemStack createOrbOfBinding() {
        ItemStack orb = new ItemStack(orbOfBindingType);
        ItemMeta meta = orb.getItemMeta();
        meta.displayName(ORB_OF_BINDING_NAME);
        if (!flammable)
            meta.setDamageResistant(DamageTypeTags.IS_FIRE);
        orb.setItemMeta(meta);
        return orb;
    }

    /**
     * Create and register the recipes for crafting and applying orbs of
     * binding.
     */
    private void registerRecipes() {
        // Recipe for crafting an orb of binding.
        ShapelessRecipe orbOfBindingRecipe = new ShapelessRecipe(
            new NamespacedKey(plugin, "orb_of_binding_craft"), orbOfBinding);

        for (Material ingredient : ingredients)
            orbOfBindingRecipe.addIngredient(ingredient);

        plugin.getServer().addRecipe(orbOfBindingRecipe, true);

        ShapelessRecipe upgradeRecipe = new ShapelessRecipe(
                new NamespacedKey(plugin, "orb_of_binding_upgrade"),
                new ItemStack(Material.STONE, 1));

        upgradeRecipe.addIngredient(new RecipeChoice.MaterialChoice(
            forge.enhanceableMaterials().stream().collect(
                Collectors.toList())));
        upgradeRecipe.addIngredient(orbOfBinding);

        plugin.getServer().addRecipe(upgradeRecipe);
    }

    /**
     * Add sockets to an item when combined with one or more orbs of binding in
     * a crafting table.
     *
     * @param event The event
     */
    @EventHandler(ignoreCancelled=true)
    public void handleCraft(PrepareItemCraftEvent event) {
        int orbs = 0;
        EnhancedItem itemToUpgrade = null;
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null)
                continue;

            if (item.isSimilar(orbOfBinding))
                orbs++;
            else
                itemToUpgrade = forge.create(item.clone());
        }

        if (orbs < 1 || itemToUpgrade == null)
            return;

        if (itemToUpgrade.socketLimit() >=
            itemToUpgrade.sockets() + orbs) {
            itemToUpgrade.addSockets(orbs);
            event.getInventory().setResult(itemToUpgrade.update());
        } else {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }
}
