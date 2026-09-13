package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryCombatCheck implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public InventoryCombatCheck(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        // TODO: Implement inventory combat check logic
    }
}
