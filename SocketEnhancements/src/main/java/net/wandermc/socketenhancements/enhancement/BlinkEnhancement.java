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
package net.wandermc.socketenhancements.enhancement;

import java.util.ArrayList;
import java.util.EnumSet;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Blink enhancement.
 *
 * Allows players to instantly travel to the block they are looking at, provided
 * it is near enough, at the cost of some experience points and temporary
 * blindness.
 */
public class BlinkEnhancement implements Enhancement, Listener {
    // How many experience points this costs per use.
    private static final int COST = 16;
    // The maximum distance a player can teleport while using this.
    private static final int MAX_DISTANCE = 64;
    // All blocks that can be teleported through.
    private static final EnumSet<Material> BLINK_THROUGH_BLOCKS;

    static {
        ArrayList<Material> materials = new ArrayList<>();
        for (Material mat : Material.values()) {
            if (mat.isBlock() && !mat.isSolid())
                materials.add(mat);
        }
        BLINK_THROUGH_BLOCKS = EnumSet.copyOf(materials);
    }

    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage()
        .deserialize("<!italic><white><<dark_purple>Blink<white>>");

    private final EnhancedItemForge forge;

    public BlinkEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    /**
     * Apply the "cosmetic" effects of a blink succeeding or failing.
     *
     * The success effects are: Blind the player for 3 seconds, spawn "warped
     * spore" particles at their location and play the sound of a chorus fruit
     * teleport.
     * The failure effect is: Play the sound of a shulker being hurt. (with
     * shell closed)
     *
     * @param player the Player to apply the effects to.
     * @param success Whether the blink succeeded or not.
     */
    private static void applyCosmetics(Player player, boolean success) {
        if (success) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                60, 1, false, false));
            player.spawnParticle(Particle.WARPED_SPORE, player.getLocation(),
                10);
            player.playSound(player.getLocation(),
                Sound.ITEM_CHORUS_FRUIT_TELEPORT, 3, 10);
        } else
            player.playSound(player.getLocation(),
                Sound.ENTITY_SHULKER_HURT_CLOSED, 2, 10);
    }

    /**
     * Determine whether `context` matches the conditions for a blink.
     *
     * The conditions are as follows: Player must have interacted with the air
     * while sneaking and holding an item with this enhancement, the player
     * must not have blindness and have at least COST experience points OR be
     * in creative.
     *
     * @param context The context to check.
     * @return Whether this enhancement's effect should be run.
     */
    private boolean contextMatches(PlayerInteractEvent context) {
        Player player = context.getPlayer();

        if (!(player.isSneaking() &&
            context.getAction() == Action.RIGHT_CLICK_AIR))
            return false;

        if (!(context.hasItem() &&
            forge.create(context.getItem()).has(this)))
            return false;

        if (player.calculateTotalExperiencePoints() < COST &&
            player.getGameMode() != GameMode.CREATIVE)
            return false;

        if (player.getPotionEffect(PotionEffectType.BLINDNESS) != null)
            return false;

        return true;
    }

    /**
     * Attempt to find a safe location for a player, starting at `start`.
     *
     * @param start The original (unsafe) location.
     * @return A safe location, or `start` if one cannot be found.
     */
    private Location findSafeLocation(Location start) {
        Location safe = start.clone();

        for (double modY = 1; modY <= MAX_DISTANCE/2; modY++) {
            safe.setY(start.getY()+modY);
            if (isSafe(safe))
                return safe;

            safe.setY(start.getY()-modY);
            if (isSafe(safe))
                return safe;
        }

        return start;
    }

    /**
     * Determine whether `loc` is a safe location.
     *
     * Any location is considered 'safe' if it won't suffocate the player or
     * place them in the air, it might still dunk them in lava, for instance.
     *
     * @param loc The location to check.
     * @return Whether it is safe.
     */
    private boolean isSafe(Location loc) {
        return !loc.getBlock().getType().isSolid() &&
                !loc.getBlock().getRelative(0, 1, 0).getType().isSolid() &&
                loc.getBlock().getRelative(0, -1, 0).isSolid();
    }

    @EventHandler
    public void run(PlayerInteractEvent context) {
        if (!contextMatches(context))
            return;

        Player player = context.getPlayer();

        Location tpLocation =
            // Block player is looking at
            player.getTargetBlock(BLINK_THROUGH_BLOCKS, MAX_DISTANCE)
            // Neighbouring block nearest to player (may be air)
            .getRelative(player.getFacing().getOppositeFace())
                .getLocation().toCenterLocation();

        // Let's at least *try* not to murder the player.
        if (!isSafe(tpLocation)) {
            Location newLocation = findSafeLocation(tpLocation);
            if (tpLocation.equals(newLocation)) {
                applyCosmetics(player, false);
                return;
            } else
                tpLocation = newLocation;
        }

        // Teleporting a player resets their yaw and pitch, so let's save them
        // first.
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        player.teleport(tpLocation);
        applyCosmetics(player, true);

        player.setRotation(yaw, pitch);

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setExperienceLevelAndProgress(
                player.calculateTotalExperiencePoints()-COST);
        }
    }

    public String name() {
        return "blink";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return !item.has("boost");
    }
}
