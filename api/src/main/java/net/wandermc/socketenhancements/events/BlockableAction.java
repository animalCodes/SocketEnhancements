package net.wandermc.socketenhancements.events;

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

/**
 * An "action" that can be blocked by an ItemEventBlocker.
 */
public enum BlockableAction {
    /**
     * A block being placed.
     */
    BLOCK_PLACE (BlockPlaceEvent.class),
    /**
     * An item being used as fuel in a brewing stand.
     */
    BREW_FUEL (BrewingStandFuelEvent.class),
    /**
     * An item being used as an ingredient in a brewing stand.
     */
    BREW_INGREDIENT (BrewEvent.class),
    /**
     * An item being used as fuel in a furnace, smoker or blast furnace.
     */
    BURN (FurnaceBurnEvent.class),
    /**
     * An item being combined with another item in an anvil.
     */
    COMBINE (PrepareAnvilEvent.class),
    /**
     * An item being placed on a campfire.
     */
    COOK (PlayerInteractEvent.class),
    /**
     * An item being enchanted.
     */
    ENCHANT (PrepareItemEnchantEvent.class),
    /**
     * An entity being placed.
     *
     * Used for end crystals, item frames and the like.
     */
    ENTITY_PLACE (EntityPlaceEvent.class),
    /**
     * An item being repaired / disenchanted in a grindstone.
     */
    GRIND (PrepareGrindstoneEvent.class),
    /**
     * An item being smelted in a furnace, smoker or blast furnace.
     */
    SMELT (FurnaceSmeltEvent.class),
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
}

