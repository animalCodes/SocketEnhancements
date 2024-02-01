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
package net.wandermc.enhancements.events;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.wandermc.enhancements.enhancement.ActiveEnhancement;

public class AggregateEventListener<C extends Event> implements Listener {
    private ArrayList<ActiveEnhancement<C>> enhancements;
    private Class<C> eventType;

    // Ignore "unchecked cast" warning caused by `Class<? extends Event>` being cast
    // to `Class<C>` (these are the same)
    @SuppressWarnings("unchecked")
    public AggregateEventListener(ActiveEnhancement<C> initialEnhancement) {
        this.enhancements = new ArrayList<>();
        this.enhancements.add(initialEnhancement);
        // Store the Event type in a Class so we can access it at runtime
        this.eventType = (Class<C>) initialEnhancement.getClass().getAnnotation(EventType.class).value();
    }

    /**
     * Get's this Aggregate's event type - the event it will listen to.
     *
     * @return The Event wrapped in a Class<> and tied with a ribbon.
     */
    public Class<C> getEventType() {
        return eventType;
    }

    /**
     * Gets this Aggregate's handler Method.
     *
     * @return The handler.
     */
    public Method getHandler() {
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class))
                return method;
        }

        System.err.println("WARNING: unable to find AggregateEventListener's handler method, everything is about to break!");

        return null; // Oh boy
    }

    /**
     * Adds an enhancement to this Aggregate.
     * Note that this will *not* add enhancements that are already in this
     * Aggregate.
     *
     * @param enhancement The ActiveEnhancement to add.
     * @return Whether the enhancement was successfully added. (see above)
     */
    public boolean add(ActiveEnhancement<C> enhancement) {
        for (ActiveEnhancement<C> e : enhancements) {
            // Bail out if a there is already an instance of `enhancement`
            // For example, we don't want 2 Protected instances.
            if (e.getClass() == enhancement.getClass())
                return false;
        }
        enhancements.add(enhancement);
        return true;
    }

    @EventHandler // Please don't touch this line
    public void handle(C event) {
        enhancements.forEach(enhancement -> {
            if (enhancement.shouldRun(event))
                enhancement.runEffect(event);
        });
    }
}
