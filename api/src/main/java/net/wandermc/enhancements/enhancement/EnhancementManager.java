package net.wandermc.enhancements.enhancement;

import java.util.HashMap;

/**
 * Class for storing an instance of each enhancement etc.
 */
public class EnhancementManager {
    private final HashMap<String, Enhancement> enhancementStore = new HashMap<String, Enhancement>();
    private final EmptySocket emptySocket = new EmptySocket();

    public EnhancementManager() {}

    /**
     * Normalises `name` to ease storage and retrieval of enhancements.
     *
     * @param name Starting name
     * @return Normalised version of `name`
     */
    private String normaliseName(String name) {
        // TODO expand this
        return name.toLowerCase();
    }

    /**
     * Stores a copy of `enhancement` under a normalised version of it's
     * `.getName()`.
     * 
     * @param enhancement The enhancement to store
     */
    public void store(Enhancement enhancement) {
        enhancementStore.put(normaliseName(enhancement.getName()), enhancement);
    }

    /**
     * Retrieves the enhancement stored under `name`.
     * If the enhancement doesn't exist, an EmptySocket will be returned
     * instead.
     *
     * @param name The name of the enhancement
     * @return The enhancement, or null if it doesn't exist.
     */
    public Enhancement get(String name) {
        return enhancementStore.getOrDefault(normaliseName(name), emptySocket);
    }
}
