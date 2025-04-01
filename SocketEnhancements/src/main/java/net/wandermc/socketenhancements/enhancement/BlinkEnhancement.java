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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import static com.destroystokyo.paper.MaterialTags.ARMOR;

import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;

/**
 * Blink enhancement.
 *
 * Allows players to instantly travel to the block they are looking at, provided
 * it is near enough, at the cost of some experience points and temporary
 * blindness.
 */
public class BlinkEnhancement implements ActiveEnhancement {
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

    private final Material costType;
    private final int costAmount;
    private final int maxDistance;

    private final EnhancedItemForge forge;

    /**
     * Create a BlinkEnhancement.
     *
     * `config` defaults:
     * cost_type: "AIR"
     * cost_amount: 16
     * max_distance: 64
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public BlinkEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        // AIR indicates experience points.
        Material mat = Material.getMaterial(config.getString("cost_type",
            "AIR"));
        if (mat == null)
            mat = Material.AIR;
        this.costType = mat;

        this.costAmount = config.getInt("cost_amount", 16);
        this.maxDistance = config.getInt("max_distance", 64);
    }

    @EventHandler(ignoreCancelled=false)
    public void run(PlayerInteractEvent context) {
        if (!contextMatches(context))
            return;

        Player player = context.getPlayer();

        Location tpLocation = player.getTargetBlock(BLINK_THROUGH_BLOCKS,
            maxDistance).getRelative(player.getFacing().getOppositeFace())
            .getLocation().toCenterLocation();

        if (!isSafe(tpLocation)) {
            Location newLocation = findSafeLocation(tpLocation);
            if (tpLocation.equals(newLocation)) {
                applyFailureCosmetics(player);
                return;
            } else
                tpLocation = newLocation;
        }

        tpLocation.setYaw(player.getLocation().getYaw());
        tpLocation.setPitch(player.getLocation().getPitch());

        player.teleport(tpLocation, TeleportCause.PLUGIN);
        applySuccessCosmetics(player);

        if (costType == Material.AIR) {
            player.setExperienceLevelAndProgress(
                player.calculateTotalExperiencePoints()-costAmount);
        } else {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            offhand.setAmount(offhand.getAmount()-costAmount);
        }
    }

    /**
     * Determine whether `context` matches the conditions for a blink.
     *
     * The conditions are as follows:
     * - Player must have interacted with the air while sneaking and holding an
     *   item with this enhancement.
     * - Player must not have blindness.
     * - If costAmount > 0. Player must be holding at least `costAmount` items
     *   of type `costType` in their offhand. OR, if costType is EXP. They must
     *   have at least `costAmount` experience points.
     *
     * @param context The context to check.
     * @return Whether this enhancement's effect should be run.
     */
    private boolean contextMatches(PlayerInteractEvent context) {
        Player player = context.getPlayer();

        if (!(player.isSneaking() &&
            context.getAction() == Action.RIGHT_CLICK_AIR))
            return false;

        if (!(context.hasItem() && forge.has(context.getItem(), this)))
            return false;

        if (player.getPotionEffect(PotionEffectType.BLINDNESS) != null) {
            applyFailureCosmetics(player);
            return false;
        }

        if (costAmount <= 0)
            return true;

        if (costType == Material.AIR) {
            if (player.calculateTotalExperiencePoints() < costAmount) {
                applyFailureCosmetics(player);
                return false;
            }
            return true;
        } else {
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (offhand.getType() != costType || offhand.getAmount() <
                costAmount) {
                applyFailureCosmetics(player);
                return false;
            }
            return true;
        }
    }

    /**
     * Apply the cosmetic effects of a blink succeeding.
     *
     * The effects are: Blind the player for 3 seconds, spawn "warped spore"
     * particles at their location and play the sound of a chorus fruit
     * teleport.
     *
     * @param player the Player to apply the effects to.
     */
    private static void applySuccessCosmetics(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
            60, 1, false, false));
        player.getWorld().spawnParticle(Particle.WARPED_SPORE,
            player.getLocation(), 10);
        player.getWorld().playSound(player.getLocation(),
            Sound.ITEM_CHORUS_FRUIT_TELEPORT, 3, 10);
    }

    /**
     * Apply the cosmetic effect of a blink failing.
     *
     * The effect is: Play the sound of a shulker being hurt. (with shell
     * closed)
     *
     * @param player the Player to apply the effects to.
     */
    private static void applyFailureCosmetics(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT_CLOSED,
            2, 10);
    }

    /**
     * Attempt to find a safe location for a player, starting at `start`.
     *
     * @param start The original (unsafe) location.
     * @return A safe location, or `start` if one cannot be found.
     */
    private Location findSafeLocation(Location start) {
        Location safe = start.clone();

        for (double modY = 1; modY <= maxDistance/2; modY++) {
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
        return !item.has("boost") &&
            !ARMOR.isTagged(item.itemStack().getType())
            && item.itemStack().getType() != Material.ELYTRA;
    }
}
