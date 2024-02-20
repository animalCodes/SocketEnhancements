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

import net.wandermc.socketenhancements.gear.EnhancedItem;

/**
 * Parent interface for different variants of Enhancements
 */
public interface Enhancement {
    /**
     * The name of this Enhancement, should be comprised of lowercase alphabetical
     * characters, with underscores for spacing. (snake_case, [a-z_])
     *
     * @return The name of this enhancement.
     */
    public String getName();

    /**
     * The lore message that will be displayed on items with this Enhancement.
     *
     * @return The socket message.
     */
    public TextComponent getSocketMessage();

    /**
     * How rare this Enhancement is.
     * See EnhancementRarity itself for more information on how this should be used.
     *
     * @return The rarity.
     */
    public EnhancementRarity getRarity();

    /**
     * Whether players should be allowed to bind this Enhancement to `item`.
     * That the item has an empty socket and that it doesn't already have this
     * enhancement is implied.
     *
     * @param item The item to check
     * @return Whether this enhancement can be bound to `item`.
     */
    public boolean isValidItem(EnhancedItem item);
}
