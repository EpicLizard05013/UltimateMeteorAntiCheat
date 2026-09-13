package net.meteorsmp.anticheat.listeners;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PacketFloodGuard implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public PacketFloodGuard(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInvalidSlotClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Detect crash exploit attempts using out-of-bounds inventory slot indexes
        if (event.getRawSlot() < -999 || event.getRawSlot() > 2000) {
            event.setCancelled(true);
            plugin.getViolationManager().addViolation(player, "PacketFlood (InvalidSlot)", 5);
            plugin.getLogger().warning("[MeteorAC-Security] Intercepted illegal slot index " + event.getRawSlot() + " from " + player.getName());
        }
    }
}
