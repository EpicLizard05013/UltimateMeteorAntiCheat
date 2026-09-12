package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldChecks implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Long> placeHistory = new ConcurrentHashMap<>();

    public WorldChecks(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (placeHistory.containsKey(uuid)) {
            long delay = now - placeHistory.get(uuid);
            if (delay < 40) {
                event.setCancelled(true);
                plugin.getViolationManager().flag(player, "World", "FastPlace (Type A)", 1.0);
            }
        }
        placeHistory.put(uuid, now);

        if (player.getPitch() > 82.0f && player.isSprinting() && event.getBlockAgainst().getY() < player.getLocation().getY()) {
            plugin.getViolationManager().flag(player, "World", "Scaffold (Type A)", 1.5);
        }
    }
}
