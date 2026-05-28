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
 * A certain number of experience points.
 */
public class CostExperiencePoints implements Cost {
    private final int amount;

    /**
     * Create a CostExperiencePoints.
     *
     * @param amount The number of experience points.
     */
    public CostExperiencePoints(int amount) {
        this.amount = amount;
    }

    /**
     * Whether `player` has enough experience points.
     *
     * @param player The player to check.
     * @return Whether they have enough points.
     */
    public boolean met(Player player) {
        return player.calculateTotalExperiencePoints() >= amount;
    }

    /**
     * Reduce player's experience point count by 'amount'.
     * It is assumed that they have at least 'amount' points.
     *
     * @param player The player to charge.
     */
    public void take(Player player) {
        player.setExperienceLevelAndProgress(
            player.calculateTotalExperiencePoints() - amount);
    }
}
