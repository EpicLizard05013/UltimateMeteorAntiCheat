package net.meteorsmp.anticheat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.block.Container;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketDesyncListener implements Listener {

    private final Map<UUID, Long> lastClickTime = new HashMap<>();
    private static final long MIN_CLICK_INTERVAL_MS = 50; // Reject packet bursts under 50ms

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        long now = System.currentTimeMillis();
        long lastClick = lastClickTime.getOrDefault(player.getUniqueId(), 0L);
        long delta = now - lastClick;

        // Detect delayed packet flushing (multiple clicks arriving in a single tick / microsecond burst)
        if (delta < MIN_CLICK_INTERVAL_MS) {
            event.setCancelled(true);
            player.closeInventory();
            player.updateInventory(); // Force client resync to clear ghost items
            return;
        }

        lastClickTime.put(player.getUniqueId(), now);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onContainerOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            // Force resync when opening any container to wipe local client ghost stacks
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onContainerBreak(BlockBreakEvent event) {
        // Prevent container dupes when breaking tile entities during pending transactions
        if (event.getBlock().getState() instanceof Container container) {
            container.getInventory().getViewers().forEach(human -> {
                if (human instanceof Player player) {
                    player.closeInventory();
                    player.updateInventory();
                }
            });
        }
    }
}
