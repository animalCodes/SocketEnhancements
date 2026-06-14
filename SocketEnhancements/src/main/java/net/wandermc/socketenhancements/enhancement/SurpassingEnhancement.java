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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;

import static io.papermc.paper.tag.BaseTag.ITEMS_PICKAXES;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.util.cost.CostExperiencePoints;
import net.wandermc.socketenhancements.util.cost.CostItemDamage;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Surpassing enhancement.
 *
 * Allows bedrock blocks to be broken. Costs experience points, damages pickaxe
 * and may be consumed in the process.
 */
public class SurpassingEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage().deserialize(
            "<!italic><white><<black><shadow:grey>Surpassing<white>>");

    private static final ItemStack bedrock = new ItemStack(Material.BEDROCK);

    private final CostExperiencePoints experienceCost;
    private final CostItemDamage damageCost;

    private final boolean singleUse;

    private final EnhancedItemForge forge;

    /*
    // Yeah turns out 'Bedrock Fragments' are quite overpowered, so remove
    // them for now but retain most of the code so it can be reused when I
    // figure out something more balanced.

    private static final TextComponent FRAGMENT_NAME = (TextComponent)
        MiniMessage.miniMessage().deserialize(
            "<!italic><aqua>Bedrock Fragment");

    /**
     * Create a Bedrock Fragment.
     *
     * @param mat Material type of fragment.
     * @return A Bedrock fragment
     */
    /*
    private static ItemStack createFragment(Material mat) {
        ItemStack fragment = new ItemStack(mat);

        ItemMeta meta = fragment.getItemMeta();
        meta.displayName(FRAGMENT_NAME);
        meta.setDamageResistant(DamageTypeTags.IS_FIRE);
        fragment.setItemMeta(meta);

        return fragment;
    }

    /**
     * Create and register the recipe for adding Bedrock Fragments to items.
     *
     * @param plugin The plugin to register the recipe under.
     * @param fragment Reference Bedrock Fragment.
     */
    /*
    private static void registerRecipe(JavaPlugin plugin, ItemStack fragment) {
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(
            new NamespacedKey(plugin, "bedrock_fragment_addition"),
            new ItemStack(Material.STONE, 1), RecipeChoice.empty(),
            new RecipeChoice.MaterialChoice(Tag.ITEMS_ENCHANTABLE_DURABILITY),
            new RecipeChoice.ExactChoice(fragment));

        plugin.getServer().addRecipe(recipe);
    }

    /**
     * Make items 'unbreakable' when upgraded with a Bedrock Fragment.
     * Items must not be unbreakable or Protected.
     *
     * @param event The event.
     */
    /*
    @EventHandler(ignoreCancelled=true)
    public void handleUpgrade(PrepareSmithingEvent event) {
        ItemStack equipment = event.getInventory().getInputEquipment();
        ItemStack upgrade = event.getInventory().getInputMineral();

        if (equipment != null && upgrade != null &&
            upgrade.isSimilar(fragment)) {
            ItemStack result = equipment.clone();

            ItemMeta meta = result.getItemMeta();

            // Don't apply if item is Protected or already unbreakable
            if (forge.has(result, "protected") || meta.isUnbreakable()) {
                event.setResult(new ItemStack(Material.AIR));
                return;
            }

            meta.setUnbreakable(true);
            result.setItemMeta(meta);

            event.setResult(result);
        }
    }
    */

    /**
     * Create a SurpassingEnhancement.
     *
     * `config` defaults:
     * single_use: true
     * experience_cost: 32
     * damage_cost: 12
     *
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public SurpassingEnhancement(EnhancedItemForge forge, ConfigurationSection
        config) {
        this.forge = forge;

        this.singleUse = config.getBoolean("single_use", true);

        int experience = config.getInt("experience_cost", 32);
        if (experience < 0)
            experience = 32;

        this.experienceCost = new CostExperiencePoints(experience);

        int damage = config.getInt("damage_cost", 12);
        if (damage < 0)
            damage = 12;

        this.damageCost = new CostItemDamage(damage, true);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(BlockBreakProgressUpdateEvent context) {
        if (context.getBlock().getType() != Material.BEDROCK)
            return;

        if (context.getEntity() instanceof Player player) {
            ItemStack pickaxe = player.getInventory().getItemInMainHand();

            if (pickaxe.isEmpty() || !forge.has(pickaxe, this))
                return;

            if (!experienceCost.met(player) || !damageCost.met(pickaxe))
                return;

            Block block = context.getBlock();

            block.breakNaturally();

            block.getWorld().playSound(block.getLocation(),
                Sound.ENTITY_WARDEN_ROAR, SoundCategory.NEUTRAL, 2, 5);
            block.getWorld().spawnParticle(Particle.ITEM, block.getLocation(),
                12, bedrock);

            if (singleUse) {
                EnhancedItem enhancedPickaxe = forge.create(pickaxe);
                enhancedPickaxe.remove(this);
                enhancedPickaxe.update();
            }

            experienceCost.take(player);
            damageCost.take(pickaxe);
        }
    }

    public String name() {
        return "surpassing";
    }

    public TextComponent socketMessage() {
        return socketMessage;
    }

    public EnhancementRarity rarity() {
        return EnhancementRarity.III;
    }

    public boolean isValidItem(EnhancedItem item) {
        return !item.has("capturing") && ITEMS_PICKAXES.isTagged(
            item.itemStack().getType());
    }
}
