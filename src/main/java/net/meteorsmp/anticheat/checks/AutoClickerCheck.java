package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class AutoClickerCheck implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, List<Long>> clickData = new HashMap<>();

    public AutoClickerCheck(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> clicks = clickData.computeIfAbsent(uuid, k -> new ArrayList<>());
        clicks.add(now);

        // Keep last 20 click timestamps (~1-2 seconds of clicking)
        if (clicks.size() > 20) {
            clicks.remove(0);
        }

        if (clicks.size() == 20) {
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < clicks.size(); i++) {
                intervals.add(clicks.get(i) - clicks.get(i - 1));
            }

            double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
            double variance = intervals.stream().mapToDouble(i -> Math.pow(i - mean, 2)).sum() / intervals.size();
            double stdDev = Math.sqrt(variance);

            // Standard deviation under 8ms with high CPS indicates automated macro/autoclicker
            double cps = 1000.0 / mean;
            if (cps > 12.0 && stdDev < 8.0) {
                plugin.getViolationManager().addViolation(player, "AutoClicker (Consistency)", 1);
                clicks.clear();
            }
        }
    }
}
