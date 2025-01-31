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
package net.wandermc.socketenhancements.events;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import static org.bukkit.Tag.ITEMS_BOATS;

import static com.destroystokyo.paper.MaterialTags.ENCHANTABLE;
import static com.destroystokyo.paper.MaterialTags.SPAWN_EGGS;

/**
 * An "action" (event involving an item) that can be blocked by an
 * ItemEventBlocker.
 */
public enum BlockableAction {
    /**
     * A block being placed.
     */
    BLOCK_PLACE(BlockPlaceEvent.class),

    /**
     * An item being used as fuel in a brewing stand.
     */
    FUEL_BREWING(BrewingStandFuelEvent.class),

    /**
     * An item being used as an ingredient in a brewing stand.
     */
    BREW_INGREDIENT(BrewEvent.class),

    /**
     * An item being used as fuel in a furnace, smoker or blast furnace.
     */
    BURN(FurnaceBurnEvent.class),

    /**
     * An item being combined with another item in an anvil.
     */
    COMBINE(PrepareAnvilEvent.class),

    /**
     * An item being placed on a campfire.
     */
    COOK(PlayerInteractEvent.class),

    /**
     * An item being enchanted.
     */
    ENCHANT(PrepareItemEnchantEvent.class),

    /**
     * An entity being placed.
     */
    ENTITY_PLACE(EntityPlaceEvent.class),

    /**
     * An entity being spawned.
     */
    ENTITY_SPAWN(PlayerInteractEvent.class),

    /**
     * An item being repaired / disenchanted in a grindstone.
     */
    GRIND(PrepareGrindstoneEvent.class),

    /**
     * An item being smelted in a furnace, smoker or blast furnace.
     */
    SMELT(FurnaceSmeltEvent.class),

    /**
     * An item being used in any crafting recipe.
     */
    USE_IN_RECIPE(PrepareItemCraftEvent.class);

    private final Class<? extends Event> eventType;

    BlockableAction(Class<? extends Event> eventType) {
        this.eventType = eventType;
    }

    public Class<? extends Event> eventType() {
        return this.eventType;
    }

    /**
     * Whether the action "PLACE_BLOCK" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canBlockPlace(Material mat) {
        return mat.isBlock();
    }

    /**
     * Whether the action "FUEL_BREWING" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canFuelBrewing(Material mat) {
        return mat == Material.BLAZE_POWDER;
    }

    /**
     * Whether the action "BREW_INGREDIENT" can be performed on `mat`.
     *
     * Will appear to work at first, but on completion progress bar will reset
     * and item will remain.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canBrewIngredient(Material mat) {
        switch (mat) {
            case NETHER_WART, REDSTONE, GLOWSTONE_DUST, FERMENTED_SPIDER_EYE,
            GUNPOWDER, DRAGON_BREATH, SUGAR, RABBIT_FOOT,
            GLISTERING_MELON_SLICE, SPIDER_EYE, PUFFERFISH, MAGMA_CREAM,
            GOLDEN_CARROT, BLAZE_POWDER, GHAST_TEAR, TURTLE_HELMET,
            PHANTOM_MEMBRANE, BREEZE_ROD, STONE, COBWEB, SLIME_BLOCK:
                return true;
            default:
                return false;
        }
    }

    /**
     * Whether the action "BURN" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canBurn(Material mat) {
        return mat.isFuel();
    }

    /**
     * Whether the action "COMBINE" can be performed on `mat`.
     *
     * Any item can be renamed in an anvil, so this will return true unless the
     * material is AIR.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canCombine(Material mat) {
        return mat != Material.AIR;
    }

    /**
     * Whether the action "COOK" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canCook(Material mat) {
        switch (mat) {
            case BEEF, CHICKEN, RABBIT, PORKCHOP, MUTTON, COD, SALMON, POTATO,
            KELP:
                return true;
            default:
                return false;
        }
    }

    /**
     * Whether the action "ENCHANT" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canEnchant(Material mat) {
        return ENCHANTABLE.isTagged(mat);
    }

    /**
     * Whether the action "ENTITY_PLACE" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canEntityPlace(Material mat) {
        if (mat == Material.ARMOR_STAND || mat == Material.END_CRYSTAL
            || ITEMS_BOATS.isTagged(mat))
            return true;
        return mat.toString().contains("MINECART");
    }

    /**
     * Whether the action "ENTITY_SPAWN" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canEntitySpawn(Material mat) {
        switch (mat) {
            case EGG, ENDER_EYE, ENDER_PEARL, ITEM_FRAME,
            GLOW_ITEM_FRAME, PAINTING, SPLASH_POTION, LINGERING_POTION:
                return true;
            default: return SPAWN_EGGS.isTagged(mat);
        }
    }

    /**
     * Whether the action "GRIND" can be performed on `mat`.
     *
     * As any item can have an enchantment applied to it, any item can have an
     * enchantment *removed* from it. So provided `mat` isn't AIR, this will
     * return true.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canGrind(Material mat) {
        return mat != Material.AIR;
    }

    /**
     * Whether the action "SMELT" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canSmelt(Material mat) {
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        Recipe recipe;
        while (iterator.hasNext()) {
            recipe = iterator.next();
            if (recipe instanceof CookingRecipe &&
                !(recipe instanceof CampfireRecipe)) {
                    if (((CookingRecipe<?>)recipe)
                        .getInputChoice().test(new ItemStack(mat)))
                            return true;
                }
        }
        return false;
    }

    /**
     * Whether the action "USE_IN_RECIPE" can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return Whether the action can be performed.
     */
    public static boolean canUseInRecipe(Material mat) {
        ItemStack matItem = new ItemStack(mat);

        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        Recipe recipe;
        while (iterator.hasNext()) {
            recipe = iterator.next();
            if (recipe instanceof ShapelessRecipe) {
                for (RecipeChoice choice : ((ShapelessRecipe)recipe)
                    .getChoiceList()) {
                    if (choice.test(matItem))
                        return true;
                }
            } else if (recipe instanceof ShapedRecipe) {
                for (RecipeChoice choice : ((ShapedRecipe)recipe)
                    .getChoiceMap().values()) {
                    if (choice == null)
                        continue;
                    if (choice.test(matItem))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine all BlockableActions that can be performed on `mat`.
     *
     * @param mat The Material to check.
     * @return All actions that can be performed on `mat`.
     */
    public static ArrayList<BlockableAction> getValidActions(Material mat) {
        ArrayList<BlockableAction> actions = new ArrayList<>();

        if (canBlockPlace(mat))
            actions.add(BLOCK_PLACE);

        if (canFuelBrewing(mat))
            actions.add(FUEL_BREWING);

        if (canBrewIngredient(mat))
            actions.add(BREW_INGREDIENT);

        if (canBurn(mat))
            actions.add(BURN);

        if (canCombine(mat))
            actions.add(COMBINE);

        if (canCook(mat))
            actions.add(COOK);

        if (canEnchant(mat))
            actions.add(ENCHANT);

        if (canEntityPlace(mat))
            actions.add(ENTITY_PLACE);

        if (canEntitySpawn(mat))
            actions.add(ENTITY_SPAWN);

        if (canGrind(mat))
            actions.add(GRIND);

        if (canSmelt(mat))
            actions.add(SMELT);

        if (canUseInRecipe(mat))
            actions.add(USE_IN_RECIPE);

        return actions;
    }
}
