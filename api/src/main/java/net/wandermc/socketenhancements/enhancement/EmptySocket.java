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

import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Enhancement representing an empty socket.
 *
 * Note that due to representing an empty socket. (rather than a filled one)
 * This breaks a few rules that normal Enhancements must adhere to, and should
 * **not** be used as a reference. If you need one, please use
 * ProtectedEnhancement.
 */
public class EmptySocket implements Enhancement {
    private TextComponent socketMessage;

    /**
     * Create an EmptySocket.
     *
     * `config` should have an "empty_socket_message" Rich Message (String)
     * field.
     *
     * @param config Configuration options for EmptySocket.
     */
    public EmptySocket(ConfigurationSection config) {
        this.socketMessage = (TextComponent) config.getRichMessage(
            "empty_socket_message", MiniMessage.miniMessage().deserialize(
            "<!italic><white><Empty Socket>"));
    }

    public String name() {
        return "";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.IMPOSSIBLE;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.socketLimit() > item.sockets();
    }
}
