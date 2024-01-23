package net.wandermc.enhancements.enhancements;

import org.bukkit.Material;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import net.wandermc.enhancements.enhancement.Enhancement;

/**
 * Nonsensical Enhancement implementation for testing only.
 */
public class DummyEnhancement implements Enhancement {
    public String getName() {
        return "Dummy";
    }

    public TextComponent getSocketMessage() {
        return Component.text("Dummy");
    }

    public boolean isValidGear(Material gear) {
        return gear == Material.DIAMOND_PICKAXE;
    }
}
