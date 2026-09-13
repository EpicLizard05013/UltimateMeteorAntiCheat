package net.meteorsmp.anticheat.antidupe;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemSanitizerGuard implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public ItemSanitizerGuard(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        if (item.getAmount() > item.getMaxStackSize()) {
            event.setCancelled(true);
            event.setCurrentItem(null);
            
            Player player = (Player) event.getWhoClicked();
            plugin.getViolationManager().addViolation(player, "IllegalStack (" + item.getType() + ")", 3);
            plugin.getLogger().warning("[MeteorAC-Sanitizer] Removed illegal stack (" + item.getAmount() + "x) from " + player.getName());
        }
    }
}
