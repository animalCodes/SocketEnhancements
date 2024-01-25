package net.wandermc.enhancements.enhancement;

import net.kyori.adventure.text.TextComponent;

import net.wandermc.enhancements.config.Settings;
import net.wandermc.enhancements.gear.EnhancedItem;

/**
 * Enhancement representing an empty socket.
 */
public class EmptySocket implements Enhancement {
    public String getName() {
        return "";
    }

    public TextComponent getSocketMessage() {
        return Settings.EMPTY_SOCKET_MESSAGE;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.getSocketLimit() > item.getSockets();
    }
}

