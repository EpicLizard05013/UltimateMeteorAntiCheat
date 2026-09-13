package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementChecks implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public MovementChecks(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.isFlying()) {
            return;
        }

        String worldName = player.getWorld().getName();

        // 1. Fly Check
        if (plugin.getConfigManager().isCheckEnabled("movement", "fly")) {
            if (!plugin.getConfigManager().isWorldBypassed("fly", worldName) && !player.hasPermission("meteorac.bypass.movement")) {
                double yDiff = event.getTo().getY() - event.getFrom().getY();
                double maxY = plugin.getConfigManager().getConfig().getDouble("checks.movement.fly.max-vertical-speed", 0.42);

                if (yDiff > maxY && !player.isGliding()) {
                    event.setTo(event.getFrom());
                    plugin.getViolationManager().addViolation(player, "Fly", 1.0);
                }
            }
        }

        // 2. Speed Check
        if (plugin.getConfigManager().isCheckEnabled("movement", "speed")) {
            if (!plugin.getConfigManager().isWorldBypassed("speed", worldName) && !player.hasPermission("meteorac.bypass.movement")) {
                double horizontalDistance = Math.hypot(event.getTo().getX() - event.getFrom().getX(), event.getTo().getZ() - event.getFrom().getZ());
                double maxGroundSpeed = plugin.getConfigManager().getConfig().getDouble("checks.movement.speed.max-ground-speed", 0.287);

                if (horizontalDistance > maxGroundSpeed * 2.5) { 
                    event.setTo(event.getFrom());
                    plugin.getViolationManager().addViolation(player, "Speed", 1.0);
                }
            }
        }
    }
}
