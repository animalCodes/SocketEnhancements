package net.wandermc.enhancements.enhancement;

import net.kyori.adventure.text.TextComponent;

import net.wandermc.enhancements.gear.EnhancedItem;

// enhancements.enhancement.Enhancement... hmm
public interface Enhancement {
    /**
     * The unique identifier for this Enhancement, used for internal storage of Enhancement.
     *
     * @return The name of this enhancement.
     */
    public String getName();

    /**
     * What should be displayed on items with this Enhancement.
     * Essentially the user-friendly version of .getName()
     *
     * @return The socket message.
     */
    public TextComponent getSocketMessage();

    /**
     * Whether `item` is valid gear for this enhancement.
     *
     * @param gear The gear to check
     * @return Whether this enhancement can be bound to `gear`.
     */
    public boolean isValidItem(EnhancedItem item);
}
