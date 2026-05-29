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
 * A description of a 'cost' (some condition that can be met), along with
 * methods for testing and deducting said costs.
 *
 * The thing to which the cost is applied is referred to as the 'entity'. This
 * has no relation to minecraft Entities - it is simply *a thing*.
 */
public interface Cost<E> {
    /**
     * Whether `entity` can fulfill the requirements of this cost.
     *
     * @param entity The entity to check.
     * @return Whether it meets the cost.
     */
    public boolean met(E entity);

    /**
     * Deduct this cost from `entity`.
     * It is assumed that the cost can be met, so always run `met()`
     * before using this.
     *
     * @param entity The entity to charge.
     */
    public void take(E entity);
}
