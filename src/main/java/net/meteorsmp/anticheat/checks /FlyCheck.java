package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.manager.ConfigManager;
import net.meteorsmp.anticheat.manager.EnforcementState;
import net.meteorsmp.anticheat.manager.ViolationManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ground detection here does NOT rely solely on Player#isOnGround() — that
 * flag can read false for a tick on slabs, stairs, snow layers, and carpets
 * because their collision boxes aren't full blocks. Instead this scans a
 * small vertical band below the player for any solid/partial-height block,
 * so standing on a slab counts as grounded exactly like a full block does.
 *
 * Flagging requires BOTH sustained airborne time AND height gained beyond a
 * single jump — a single 0.42-block tick (a normal jump impulse) can never
 * trigger this on its own.
 */
public class FlyCheck implements Listener {

    private final ConfigManager cfg;
    private final ViolationManager violations;

    private final Map<UUID, Double> groundedY = new HashMap<>();
    private final Map<UUID, Integer> airborneTicks = new HashMap<>();

    public FlyCheck(JavaPlugin plugin, ConfigManager cfg, ViolationManager violations) {
        this.cfg = cfg;
        this.violations = violations;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!EnforcementState.isEnabled()) return;
        if (!cfg.isEnabled("movement.fly")) return;

        Player player = event.getPlayer();
        if (player.hasPermission("uac.bypass")) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding() || player.isInsideVehicle() || player.isSwimming()) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (event.getTo() == null) return;

        UUID id = player.getUniqueId();

        if (isNearGround(player.getLocation())) {
            groundedY.put(id, player.getLocation().getY());
            airborneTicks.put(id, 0);
            return;
        }

        int ticksAirborne = airborneTicks.merge(id, 1, Integer::sum);
        double baseline = groundedY.getOrDefault(id, player.getLocation().getY());
        double heightGained = player.getLocation().getY() - baseline;

        int graceTicks = cfg.getInt("movement.fly.grace-ticks", 10);       // covers a full vanilla jump's ascent
        double maxJumpHeight = cfg.getDouble("movement.fly.max-jump-height", 1.35); // vanilla jump + buffer

        if (ticksAirborne > graceTicks && heightGained > maxJumpHeight) {
            violations.addViolation(player, "Flight", 1);
        }
    }

    /** True if a solid or partial-height block exists within a small band below the player's feet. */
    private boolean isNearGround(Location loc) {
        if (loc.getBlock().isLiquid()) return true;
        double tolerance = 0.6;
        for (double dy = 0.05; dy <= tolerance; dy += 0.15) {
            Material type = loc.clone().subtract(0, dy, 0).getBlock().getType();
            String name = type.name();
            if (type.isSolid() || name.contains("SLAB") || name.contains("STAIRS")
                    || name.contains("CARPET") || type == Material.SNOW) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        groundedY.remove(id);
        airborneTicks.remove(id);
    }
}
