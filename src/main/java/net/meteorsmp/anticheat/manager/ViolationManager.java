package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationManager {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Double> violationBuffers = new ConcurrentHashMap<>();

    public ViolationManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
        
        long period = 20L;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::decayBuffers, period, period);
    }

    public void flag(Player player, String category, String checkType, double bufferValue) {
        if (plugin.getWhitelistManager().isWhitelisted(player.getUniqueId())) return;

        UUID uuid = player.getUniqueId();
        double currentBuffer = violationBuffers.getOrDefault(uuid, 0.0) + bufferValue;
        violationBuffers.put(uuid, currentBuffer);

        plugin.getAcLogger().logViolation(player, category + " -> " + checkType + " [Val: " + String.format("%.2f", currentBuffer) + "]");

        double maxAllowed = plugin.getConfig().getDouble("buffer.max-violations", 15.0);
        if (currentBuffer >= maxAllowed) {
            plugin.getPunishmentManager().punish(player, category);
            violationBuffers.put(uuid, 0.0);
        }
    }

    private void decayBuffers() {
        double decayRate = plugin.getConfig().getDouble("buffer.decay-rate", 0.5);
        violationBuffers.forEach((uuid, value) -> {
            if (value <= decayRate) {
                violationBuffers.remove(uuid);
            } else {
                violationBuffers.put(uuid, value - decayRate);
            }
        });
    }
}
