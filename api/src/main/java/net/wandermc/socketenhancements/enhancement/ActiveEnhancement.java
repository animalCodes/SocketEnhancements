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

import org.bukkit.event.Event;

/**
 * Defines enhancements that are run on an event.
 */
public interface ActiveEnhancement<C extends Event> extends Enhancement {
    /**
     * Check context and run this Enhancement's effect.
     *
     * @param context The event being handled.
     * @return Whether the effect was run successfully, if at all.
     */
    public boolean run(C context);

    /**
     * Return the class of the Event on which this enhancement should be run.
     *
     * @return Whether the effect was run successfully.
     */
    public Class<C> getEventType();
}
