package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class MovementChecks implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public MovementChecks(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getAllowFlight() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.isGliding() || player.isInsideVehicle()) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        double deltaY = to.getY() - from.getY();
        double deltaXZ = Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());
        
        Location loc = player.getLocation();
        Material blockType = loc.getBlock().getType();
        Material blockBelow = loc.clone().subtract(0, 0.5, 0).getBlock().getType();

        // Check if player is near/in water, lava, scaffolding, or ladders
        boolean inLiquid = blockType == Material.WATER || blockType == Material.LAVA || blockBelow == Material.WATER || blockBelow == Material.LAVA;
        boolean onClimbable = blockType == Material.LADDER || blockType == Material.VINE || blockType == Material.SCAFFOLDING;

        // --- FLY CHECK (Type A) ---
        // Flag zero Y-change while completely airborne in non-solid blocks
        if (deltaY == 0.0 && !blockType.isSolid() && !player.isOnGround() && !inLiquid && !onClimbable && !isNearGround(to)) {
            event.setTo(from);
            plugin.getViolationManager().flag(player, "Movement", "Fly (Type A)", 1.2);
            return;
        }

        // --- JESUS CHECK (Type A) ---
        // Flag sprinting across top liquid surfaces without submerging
        if ((blockType == Material.WATER || blockType == Material.LAVA) && player.isSprinting() && deltaY == 0.0) {
            plugin.getViolationManager().flag(player, "Movement", "Jesus (Type A)", 1.0);
        }

        // --- NOFALL CHECK (Type A) ---
        // Flag when client spoofs ground status during rapid downward fall
        if (player.isOnGround() && deltaY < -0.7 && !blockType.isSolid() && !isNearGround(to)) {
            plugin.getViolationManager().flag(player, "Movement", "NoFall (Type A)", 1.5);
        }

        // --- SPEED CHECK (Type A) ---
        // Base limit dynamic calculation incorporating walk speed attribute and potion modifiers
        double baseLimit = player.getWalkSpeed() * 2.85;
        
        // Add Speed effect compensation
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amp = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            baseLimit += (amp * 0.08);
        }
        
        // Add Ice surface momentum buffer
        if (blockBelow.name().contains("ICE")) {
            baseLimit += 0.25;
        }

        if (deltaXZ > baseLimit && player.isOnGround()) {
            event.setTo(from);
            plugin.getViolationManager().flag(player, "Movement", "Speed (Type A)", 1.0);
        }
    }

    private boolean isNearGround(Location loc) {
        // Samples 3x3 footprint box to account for standing on block edges, slabs, or stairs
        for (double x = -0.3; x <= 0.3; x += 0.3) {
            for (double z = -0.3; z <= 0.3; z += 0.3) {
                if (loc.clone().add(x, -0.2, z).getBlock().getType().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }
}
