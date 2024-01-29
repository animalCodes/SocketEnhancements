package net.wandermc.enhancements.enhancement;

import org.bukkit.event.Event;

/**
 * Defines enhancements that are run on an event.
 */
public interface ActiveEnhancement<C extends Event> extends Enhancement {
    /**
     * Determines whether the enhancement's effect should be run.
     *
     * @return Whether the enhancement's effect should be run.
     */
    public boolean shouldRun(C context);

    /**
     * Runs this Enhancement's effect.
     *
     * @return Whether the effect was run successfully.
     */
    public boolean runEffect(C context);
}
