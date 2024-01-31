package net.wandermc.enhancements.enhancements;

import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import net.wandermc.enhancements.enhancement.ActiveEnhancement;
import net.wandermc.enhancements.enhancement.EnhancementManager;
import net.wandermc.enhancements.events.EventType;
import net.wandermc.enhancements.gear.EnhancedItem;

/**
 * Protected enhancement, Stops the item from breaking but will be consumed in the process.
 */
@EventType(PlayerItemBreakEvent.class)
public class Protected implements ActiveEnhancement<PlayerItemBreakEvent> {
    private EnhancementManager manager;
    
    public Protected(EnhancementManager manager) {
        this.manager = manager;
    }

    public String getName() {
        return "protected";
    }

    public TextComponent getSocketMessage() {
        // "<Protected>" where the text "Protected" is dark gray.
        return Component.text("<", NamedTextColor.WHITE).append(Component.text("Protected", NamedTextColor.DARK_GRAY)).append(Component.text(">", NamedTextColor.WHITE));
    }

    public boolean isValidItem(EnhancedItem item) {
        // If an item can take damage, it can break.
        return item.getItemStack().getItemMeta() instanceof Damageable;
    }

    public boolean shouldRun(PlayerItemBreakEvent context)  {
        return new EnhancedItem(manager, context.getBrokenItem()).hasEnhancement(this);
    }

    public boolean runEffect(PlayerItemBreakEvent context) {
        EnhancedItem enhancedItem = new EnhancedItem(manager, context.getBrokenItem());
        enhancedItem.removeEnhancement(this);

        ItemStack itemStack = enhancedItem.getItemStack();

        // Get and update damage
        ItemMeta itemMeta = itemStack.getItemMeta();
        // Damage = how much damage item has taken so 0 damage = full durability
        // The checker already confirmed that the item's ItemMeta is an instance of Damageable, so this should be a safe cast.
        ((Damageable) itemMeta).setDamage(0);
        itemStack.setItemMeta(itemMeta);

        context.getPlayer().getInventory().addItem(itemStack);

        return true;
    }
}
