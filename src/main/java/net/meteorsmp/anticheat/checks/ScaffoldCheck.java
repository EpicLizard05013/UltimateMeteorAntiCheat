package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScaffoldCheck implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Long> lastPlaceTime = new HashMap<>();

    public ScaffoldCheck(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("meteorac.bypass.movement")) return;

        long now = System.currentTimeMillis();
        long lastTime = lastPlaceTime.getOrDefault(player.getUniqueId(), 0L);
        lastPlaceTime.put(player.getUniqueId(), now);

        long timeDiff = now - lastTime;
        float pitch = player.getLocation().getPitch();

        // Check 1: FastPlace - placing blocks faster than 45ms continuously
        if (timeDiff > 0 && timeDiff < 45) {
            plugin.getViolationManager().addViolation(player, "FastPlace (Frequency)", 1);
            event.setCancelled(true);
            return;
        }

        // Check 2: Impossible Pitch Scaffold - Placing directly under feet while looking up or flat (pitch < 30°)
        boolean blockIsBelow = event.getBlockAgainst().getRelative(BlockFace.UP).equals(event.getBlockPlaced())
                && event.getBlockPlaced().getLocation().getBlockY() < player.getLocation().getBlockY();

        if (blockIsBelow && pitch < 30.0f && !player.isGliding() && player.isSprinting()) {
            plugin.getViolationManager().addViolation(player, "Scaffold (Pitch/Angle)", 1);
            event.setCancelled(true);
        }
    }
}
