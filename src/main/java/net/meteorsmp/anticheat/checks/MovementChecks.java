package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MovementChecks implements Listener {

    private final UltimateMeoterAnticheat plugin;
    // Tracks the last timestamp (in ms) a player received explosion knockback
    private final Map<UUID, Long> explosionGracePeriod = new ConcurrentHashMap<>();

    public MovementChecks(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Catch End Crystals, TNT, Respawn Anchors, and Bed explosions
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
            event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            
            // Grant 1500ms (1.5s) immunity to movement checks for velocity processing
            explosionGracePeriod.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Bypass movement/fly checks if player recently took explosion knockback
        long lastExplosion = explosionGracePeriod.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - lastExplosion < 1500) {
            return;
        }

        // --- Your existing Fly / Speed check logic below ---
        
        /* Example Fly Check Logic:
        if (player.isFlying() || player.getAllowFlight()) return;
        
        double yDiff = event.getTo().getY() - event.getFrom().getY();
        if (yDiff > 0.5 && !player.getLocation().getBlock().getType().isSolid()) {
            plugin.getViolationManager().flag(player, "Movement", "Fly", 2.0);
        }
        */
    }
}
