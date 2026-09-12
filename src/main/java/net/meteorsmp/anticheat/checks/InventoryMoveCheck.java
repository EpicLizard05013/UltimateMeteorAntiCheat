package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.manager.ConfigManager;
import net.meteorsmp.anticheat.manager.ViolationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryMoveCheck implements Listener {

    private final ConfigManager cfg;
    private final ViolationManager violations;

    public InventoryMoveCheck(JavaPlugin plugin, ConfigManager cfg, ViolationManager violations) {
        this.cfg = cfg;
        this.violations = violations;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!cfg.isEnabled("world.inventorymove")) return;

        Player player = event.getPlayer();
        if (player.hasPermission("uac.bypass")) return;
        if (event.getTo() == null) return;

        InventoryType type = player.getOpenInventory().getType();
        if (type == InventoryType.CRAFTING || type == InventoryType.PLAYER) return;

        if (event.getFrom().distance(event.getTo()) > cfg.getDouble("world.inventorymove.epsilon", 0.03)) {
            violations.addViolation(player, "InventoryMove", 1);
        }
    }
}
