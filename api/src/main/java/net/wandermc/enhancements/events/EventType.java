package net.wandermc.enhancements.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.bukkit.event.Event;

/**
 * Associates an Event type with a class for retrieval at runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EventType {
    Class<? extends Event> value();
}
