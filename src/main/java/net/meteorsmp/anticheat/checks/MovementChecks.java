package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementChecks implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public MovementChecks(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getAllowFlight() || player.getGameMode() == GameMode.CREATIVE || player.isGliding()) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double deltaY = to.getY() - from.getY();
        double deltaXZ = Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());
        Material blockType = player.getLocation().getBlock().getType();

        if (deltaY == 0.0 && !blockType.isSolid() && !player.isOnGround() && blockType != Material.WATER) {
            event.setTo(from);
            plugin.getViolationManager().flag(player, "Movement", "Fly (Type A)", 1.2);
            return;
        }

        if ((blockType == Material.WATER || blockType == Material.LAVA) && player.isSprinting() && deltaY == 0.0) {
            plugin.getViolationManager().flag(player, "Movement", "Jesus (Type A)", 1.0);
        }

        if (player.isOnGround() && deltaY < -0.7 && !blockType.isSolid()) {
            plugin.getViolationManager().flag(player, "Movement", "NoFall (Type A)", 1.5);
        }

        double limit = player.getWalkSpeed() * 2.85;
        if (deltaXZ > limit && player.isOnGround()) {
            event.setTo(from);
            plugin.getViolationManager().flag(player, "Movement", "Speed (Type A)", 1.0);
        }
    }
}
