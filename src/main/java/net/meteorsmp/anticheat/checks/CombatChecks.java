package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatChecks implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, List<Long>> clickHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastYaw = new ConcurrentHashMap<>();

    public CombatChecks(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("checks.combat.reach", true)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (attacker.getGameMode() == GameMode.CREATIVE || attacker.getGameMode() == GameMode.SPECTATOR) return;

        Entity target = event.getEntity();
        
        // Calculate raw distance from eye location to target feet center
        double rawDistance = attacker.getEyeLocation().distance(target.getLocation());
        
        // Subtract target bounding box radius (~0.45m) to get accurate edge-to-edge distance
        double adjustedDistance = rawDistance - 0.45;

        // Dynamic ping-compensated reach threshold
        int ping = attacker.getPing();
        double baseReach = plugin.getConfig().getDouble("checks.combat.max-reach", 3.01);
        double pingBuffer = Math.min((ping / 1000.0) * 1.2, 0.35);
        double allowedReach = baseReach + pingBuffer;

        if (adjustedDistance > allowedReach) {
            event.setCancelled(true);
            double vl = Math.min(5.0, (adjustedDistance - allowedReach) * 2.5);
            plugin.getViolationManager().flag(attacker, "Combat", "Reach (Type A)", vl);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("checks.combat.autoclicker", true)) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // --- AIM MODULO CHECK ---
        float yaw = player.getLocation().getYaw();
        if (lastYaw.containsKey(uuid)) {
            float diff = Math.abs(yaw - lastYaw.get(uuid));
            // Snaps on exact integer angles (typical of low-end killaura / aimbots)
            if (diff > 0.0f && diff % 1.0f == 0.0f) {
                plugin.getViolationManager().flag(player, "Combat", "AimModulo (Type A)", 1.0);
            }
        }
        lastYaw.put(uuid, yaw);

        // --- AUTOCLICKER & CPS CHECK ---
        List<Long> clicks = clickHistory.computeIfAbsent(uuid, k -> new ArrayList<>());
        clicks.add(now);

        if (clicks.size() > 15) {
            clicks.remove(0);
            double avg = getAverage(clicks);
            
            // Prevent division by zero on identical timestamps
            if (avg > 0) {
                double stdDev = getStdDev(clicks, avg);
                double cps = 1000.0 / avg;

                if (cps > 18.0 && stdDev < 6.0) {
                    plugin.getViolationManager().flag(player, "Combat", "Autoclicker (Type A)", 1.8);
                }
            }
        }
    }

    private double getAverage(List<Long> list) {
        long sum = 0;
        for (int i = 1; i < list.size(); i++) {
            sum += (list.get(i) - list.get(i - 1));
        }
        return (double) sum / (list.size() - 1);
    }

    private double getStdDev(List<Long> list, double avg) {
        double var = 0;
        for (int i = 1; i < list.size(); i++) {
            var += Math.pow((list.get(i) - list.get(i - 1)) - avg, 2);
        }
        return Math.sqrt(var / (list.size() - 1));
    }
}
