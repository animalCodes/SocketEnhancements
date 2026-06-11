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
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.tag.DamageTypeTags;

import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;

import static io.papermc.paper.tag.BaseTag.ITEMS_PICKAXES;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.wandermc.socketenhancements.item.EnhancedItemForge.EnhancedItem;
import net.wandermc.socketenhancements.item.EnhancedItemForge;
import net.wandermc.socketenhancements.util.cost.CostExperiencePoints;
import net.wandermc.socketenhancements.util.event.BlockableAction;
import net.wandermc.socketenhancements.util.event.ItemEventBlocker;

import static net.wandermc.socketenhancements.util.Dice.roll;

/**
 * Surpassing enhancement.
 *
 * Allows bedrock blocks to be broken. May be consumed in the process and may
 * drop 'Bedrock Fragments' that can be used to make items Unbreakable.
 */
public class SurpassingEnhancement implements ActiveEnhancement {
    private static final TextComponent socketMessage = (TextComponent)
        MiniMessage.miniMessage().deserialize(
            "<!italic><white><<black><shadow:grey>Surpassing<white>>");

    private static final TextComponent FRAGMENT_NAME = (TextComponent)
        MiniMessage.miniMessage().deserialize(
            "<!italic><aqua>Bedrock Fragment");

    private static final ItemStack bedrock = new ItemStack(Material.BEDROCK);

    private final CostExperiencePoints cost;

    private final boolean singleUse;
    private final double fragmentDropChance;

    private final ItemStack fragment;

    private final EnhancedItemForge forge;
    private final ItemEventBlocker eventBlocker;

    /**
     * Create a Bedrock Fragment.
     *
     * @param mat Material type of fragment.
     * @return A Bedrock fragment
     */
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
    private static void registerRecipe(JavaPlugin plugin, ItemStack fragment) {
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(
            new NamespacedKey(plugin, "bedrock_fragment_addition"),
            new ItemStack(Material.STONE, 1), RecipeChoice.empty(),
            new RecipeChoice.MaterialChoice(Tag.ITEMS_ENCHANTABLE_DURABILITY),
            new RecipeChoice.ExactChoice(fragment));

        plugin.getServer().addRecipe(recipe);
    }

    /**
     * Create a SurpassingEnhancement.
     *
     * `config` defaults:
     * single_use: true
     * cost_amount: 32
     * fragments:
     *    material: "ECHO_SHARD"
     *    drop_chance: 0.6
     *
     * @param plugin the plugin this is registered by.
     * @param forge The current EnhancedItemForge.
     * @param config Configuration options.
     */
    public SurpassingEnhancement(JavaPlugin plugin, EnhancedItemForge forge,
        ConfigurationSection config) {
        this.forge = forge;

        this.singleUse = config.getBoolean("single_use", true);

        int costAmount = config.getInt("cost_amount", 32);
        if (costAmount < 32)
            costAmount = 32;

        this.cost = new CostExperiencePoints(costAmount);

        // Setup fragments
        ConfigurationSection fragmentConfig = config
            .getConfigurationSection("fragments");
        if (fragmentConfig == null)
            fragmentConfig = new YamlConfiguration();

        Material mat = Material.getMaterial(fragmentConfig
            .getString("material", "ECHO_SHARD"));
        if (mat == null || mat == Material.AIR)
            mat = Material.ECHO_SHARD;

        this.fragment = createFragment(mat);

        BlockableAction[] a = {};
        this.eventBlocker = new ItemEventBlocker(plugin,
            item -> fragment.isSimilar(item),
            BlockableAction.getValidActions(mat).toArray(a));

        registerRecipe(plugin, this.fragment);

        this.fragmentDropChance = fragmentConfig.getDouble("drop_chance",
            0.6);
    }

    @EventHandler(ignoreCancelled=true)
    public void run(BlockBreakProgressUpdateEvent context) {
        if (context.getBlock().getType() != Material.BEDROCK)
            return;

        if (context.getEntity() instanceof Player player) {
            ItemStack pickaxe = player.getInventory().getItemInMainHand();

            if (pickaxe.isEmpty() || !forge.has(pickaxe, this))
                return;

            if (!cost.met(player))
                return;

            Block block = context.getBlock();

            block.breakNaturally();

            block.getWorld().playSound(block.getLocation(),
                Sound.ENTITY_WARDEN_ROAR, SoundCategory.NEUTRAL, 2, 5);
            block.getWorld().spawnParticle(Particle.ITEM, block.getLocation(),
                12, bedrock);

            if (roll(fragmentDropChance))
                player.getWorld().dropItemNaturally(
                    block.getLocation(), fragment);

            if (singleUse) {
                EnhancedItem enhancedPickaxe = forge.create(pickaxe);
                enhancedPickaxe.remove(this);
                enhancedPickaxe.update();
            }

            cost.take(player);
        }
    }

    /**
     * Make items 'unbreakable' when upgraded with a Bedrock Fragment.
     * Items must not be unbreakable or Protected.
     *
     * @param event The event.
     */
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
