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
package net.wandermc.socketenhancements.util;

import java.util.List;

public class Dice {
    /**
     * Picks a random item from none-empty `list`.
     *
     * @param list The list to choose from.
     * @return A random item from `list`.
     * @throws IllegalArgumentException If `list` is empty.
     */
    public static <T> T chooseOne(List<T> list) {
        if (list.isEmpty())
            throw new IllegalArgumentException("list cannot be empty.");

        return list.get((int)(Math.random() * list.size()));
    }

    /**
     * Randomises the contents of `list`.
     *
     * @param list The list to randomise.
     */
    public static <T> void randomise(List<T> list) {
        int ri = 0;
        T temp;
        for (int i = 0; i < list.size(); i++) {
            ri = (int)Math.floor(Math.random() * list.size());
            if (ri != i) {
                temp = list.get(ri);
                list.set(ri, list.get(i));
                list.set(i, temp);
            }
        }
    }

    /**
     * Has a `percentage` chance to return true.
     *
     * @param percentage Chance as a decimal, so 0 = 0%, 1 = 100%, etc.
     * @return Result of roll.
     */
    public static boolean roll(double percentage) {
        return Math.random() < percentage;
    }
}
