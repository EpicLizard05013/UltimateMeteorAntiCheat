package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ReachCheck implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public ReachCheck(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Entity victim = event.getEntity();

        if (attacker.getGameMode() == GameMode.CREATIVE) return;

        double distance = attacker.getEyeLocation().distance(victim.getLocation());
        double maxReach = 3.15; // Vanilla survival limit with latency buffer

        if (distance > maxReach) {
            event.setCancelled(true);
            plugin.getViolationManager().addViolation(attacker, "Reach (" + String.format("%.2f", distance) + "b)", 1);
        }
    }
}
