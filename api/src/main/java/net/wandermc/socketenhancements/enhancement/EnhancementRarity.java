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

/**
 * Enum for signifying how rare (easy/hard to obtain) an Enhancement is.
 */
public enum EnhancementRarity {
    /**
     * Least rare, easy to obtain.
     */
    I,

    /**
     * Mid-tier, harder to obtain than I, but easier than III.
     */
    II,

    /**
     * Most rare, hard to obtain.
     */
    III,

    /**
     * Impossible to obtain normally, mainly used for special case
     * enhancements such as EmptySocket.
     */
    IMPOSSIBLE
}
