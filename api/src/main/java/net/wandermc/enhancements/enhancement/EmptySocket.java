/*
 *    SocketEnhancements: A basic gear enhancement plugin for PaperMC servers.
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

