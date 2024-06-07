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

import java.util.ArrayList;
import java.util.EnumSet;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import net.wandermc.socketenhancements.enhancement.ActiveEnhancement;
import net.wandermc.socketenhancements.enhancement.EnhancementRarity;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Blink enhancement, allows players to instantly travel to the block they are
 * looking at, provided it is near enough, at the cost of some experience points
 * and temporary blindness.
 */
public class BlinkEnhancement implements
    ActiveEnhancement<PlayerInteractEvent> {
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

    private final EnhancedItemForge forge;

    public BlinkEnhancement(EnhancedItemForge forge) {
        this.forge = forge;
    }

    /**
     * Apply the "cosmetic" effects of blinking onto `player`.
     *
     * Specifically, blinds the player for 3 seconds, spawns "warped spore"
     * particles at their location and plays the sound of a chorus fruit
     * teleport.
     *
     * @param player the Player to apply the effects to.
     */
    private static void applyCosmetics(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60,
            1, false, false));
        player.spawnParticle(Particle.WARPED_SPORE, player.getLocation(), 10);
        player.playSound(player.getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 3, 10);
    }

    /**
     * Determines whether `context` matches the conditions for a blink.
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
            forge.create(context.getItem()).hasEnhancement(this)))
            return false;

        if (player.calculateTotalExperiencePoints() < COST &&
            player.getGameMode() != GameMode.CREATIVE)
            return false;

        if (player.getPotionEffect(PotionEffectType.BLINDNESS) != null)
            return false;

        return true;
    }

    public boolean run(PlayerInteractEvent context) {
        if (!contextMatches(context))
            return false;

        Player player = context.getPlayer();

        Location tpLocation =
            // Block player is looking at
            player.getTargetBlock(BLINK_THROUGH_BLOCKS, MAX_DISTANCE)
            // Neighbouring block nearest to player (may be air)
            // TODO find nearest "safe" block to avoid suffocating player or
            // dumping them in lava.
            .getRelative(player.getFacing().getOppositeFace()).getLocation();

        // Teleporting a player resets their location, so let's save it first.
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        player.teleport(tpLocation);
        applyCosmetics(player);

        player.setRotation(yaw, pitch);

        if (player.getGameMode() != GameMode.CREATIVE) {
            // Two methods with very long names that are rather useful.
            player.setExperienceLevelAndProgress(
                player.calculateTotalExperiencePoints()-COST);
        }

        return true;
    }

    public String getName() {
        return "blink";
    }

    public TextComponent getSocketMessage() {
        // "<Blink>" where the text "Blink" is dark purple and the "< >"s are white.
        return Component.text("<", Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE)))
        .append(Component.text("Blink", NamedTextColor.DARK_PURPLE))
        .append(Component.text(">", NamedTextColor.WHITE));
    }

    public EnhancementRarity getRarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return !item.hasEnhancement("boost");
    }

    public Class<PlayerInteractEvent> getEventType() {
        return PlayerInteractEvent.class;
    }
}
