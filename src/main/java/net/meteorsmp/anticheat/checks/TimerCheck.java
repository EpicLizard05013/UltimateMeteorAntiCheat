package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.manager.ConfigManager;
import net.meteorsmp.anticheat.manager.ViolationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimerCheck implements Listener {

    private final ConfigManager cfg;
    private final ViolationManager violations;
    private final Map<UUID, Deque<Long>> recentMoves = new HashMap<>();

    public TimerCheck(JavaPlugin plugin, ConfigManager cfg, ViolationManager violations) {
        this.cfg = cfg;
        this.violations = violations;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!cfg.isEnabled("movement.timer")) return;

        Player player = event.getPlayer();
        if (player.hasPermission("uac.bypass")) return;

        long now = System.currentTimeMillis();
        Deque<Long> moves = recentMoves.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
        moves.addLast(now);

        while (!moves.isEmpty() && now - moves.peekFirst() > 1000) {
            moves.pollFirst();
        }

        if (moves.size() > cfg.getInt("movement.timer.max-moves-per-second", 24)) {
            violations.addViolation(player, "Timer", 1);
        }
    }
}
