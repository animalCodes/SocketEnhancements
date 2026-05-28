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
package net.wandermc.socketenhancements.util.cost;

import org.bukkit.entity.Player;

/**
 * A description of a 'cost' (some condition that a player can meet), along with
 * methods for testing and deducting said costs.
 *
 * A 'condition' is simply something that a player can possess - such as an item
 * or experience points, which can be deducted when, for example, an enhancement
 * is activated.
 */
public interface Cost {
    /**
     * Whether `player` can fulfill the requirements of this cost.
     *
     * @param player The player to check.
     * @return Whether they meet the cost.
     */
    public boolean met(Player player);

    /**
     * Deduct this cost from `player`.
     * It is assumed that the cost can be met, so always run `met()`
     * before using this.
     *
     * @param player The player to charge.
     */
    public void take(Player player);
}
