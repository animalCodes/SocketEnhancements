package net.wandermc.enhancements.enhancement;

import java.util.HashMap;

/**
 * Class for storing an instance of each enhancement etc.
 */
public class EnhancementManager {
    private static final HashMap<String, Enhancement> enhancementStore = new HashMap<String, Enhancement>();

    /**
     * Normalises `name` to ease storage and retrieval of enhancements.
     *
     * @param name Starting name
     * @return Normalised version of `name`
     */
    private static String normaliseName(String name) {
        // TODO expand this
        return name.toLowerCase();
    }

    /**
     * Stores a copy of `enhancement` under a normalised version of it's
     * `.getName()`.
     * 
     * @param enhancement The enhancement to store
     */
    public static void store(Enhancement enhancement) {
       enhancementStore.put(normaliseName(enhancement.getName()), enhancement);
    }

    /**
     * Retrieves the enhancement stored under `name`.
     * Return value may be null.
     *
     * @param name The name of the enhancement
     * @return The enhancement, or null if it doesn't exist.
     */
    public static Enhancement get(String name) {
       return enhancementStore.get(normaliseName(name));
    }
}
