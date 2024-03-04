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

import net.kyori.adventure.text.TextComponent;

import net.wandermc.socketenhancements.config.Settings;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Enhancement representing an empty socket.
 * Note that due to representing an empty socket, (rather than a filled one)
 * this breaks a few rules that normal Enhancements must adhere to, and should
 * **not** be used as a reference.
 * Instead, please use Protected for that purpose.
 */
public class EmptySocket implements Enhancement {
    public String getName() {
        return "";
    }

    public TextComponent getSocketMessage() {
        return Settings.EMPTY_SOCKET_MESSAGE;
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.IMPOSSIBLE;
    }

    public boolean isValidItem(EnhancedItem item) {
        return item.getSocketLimit() > item.getSockets();
    }
}
