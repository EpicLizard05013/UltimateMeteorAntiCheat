package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.manager.ConfigManager;
import net.meteorsmp.anticheat.manager.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KnockbackCheck implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager cfg;
    private final ViolationManager violations;
    private final Map<UUID, Vector> velocityBefore = new HashMap<>();

    public KnockbackCheck(JavaPlugin plugin, ConfigManager cfg, ViolationManager violations) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.violations = violations;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!cfg.isEnabled("combat.knockback")) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.hasPermission("uac.bypass")) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) return;

        velocityBefore.put(player.getUniqueId(), player.getVelocity().clone());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Vector before = velocityBefore.remove(player.getUniqueId());
            if (before == null || !player.isOnline()) return;

            double before2D = Math.hypot(before.getX(), before.getZ());
            double after2D = Math.hypot(player.getVelocity().getX(), player.getVelocity().getZ());
            double minExpected = cfg.getDouble("combat.knockback.min-velocity-change", 0.08);

            if (Math.abs(after2D - before2D) < minExpected && after2D < minExpected) {
                violations.addViolation(player, "AntiKnockback", 1);
            }
        }, 2L);
    }
}
