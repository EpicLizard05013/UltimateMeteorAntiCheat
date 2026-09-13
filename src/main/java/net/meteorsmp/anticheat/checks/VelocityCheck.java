package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VelocityCheck implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Long> pendingVelocityTime = new HashMap<>();

    public VelocityCheck(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        pendingVelocityTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingVelocityTime.containsKey(uuid)) return;
        long hitTime = pendingVelocityTime.get(uuid);

        if (System.currentTimeMillis() - hitTime < 300) {
            Vector movement = event.getTo().toVector().subtract(event.getFrom().toVector());
            double verticalDist = Math.abs(movement.getY());

            if (verticalDist < 0.001 && !player.isGliding() && !player.isInsideVehicle() && !player.isClimbing()) {
                plugin.getViolationManager().addViolation(player, "Velocity (AntiKB)", 1);
                pendingVelocityTime.remove(uuid);
            }
        } else {
            pendingVelocityTime.remove(uuid);
        }
    }
}
