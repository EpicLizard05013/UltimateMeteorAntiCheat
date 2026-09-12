package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class ViolationManager {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Double> violations = new ConcurrentHashMap<>();
    private final Set<UUID> frozenPlayers = new HashSet<>();

    public ViolationManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    // Resolves the flag(...) symbol error across Combat, Movement, and World checks
    public void flag(Player player, String category, String checkName, double vl) {
        UUID uuid = player.getUniqueId();
        double currentVl = violations.getOrDefault(uuid, 0.0) + vl;
        violations.put(uuid, currentVl);

        int intVl = (int) Math.round(currentVl);
        
        // Log to violations daily log file
        plugin.getAcLogger().logViolation(player.getName(), category + ":" + checkName, intVl, "+" + vl + " VL");
        
        // Check ladder punishments from punishments.yml
        plugin.getPunishmentManager().executePunishment(player.getName(), category.toLowerCase(), intVl);

        // Fallback default kick threshold
        if (currentVl >= 50.0) {
            plugin.getPunishmentManager().punish(player, "Excessive " + category + " flags (" + checkName + ")");
        }
    }

    public void addViolation(Player player, String check, int vl) {
        flag(player, "General", check, (double) vl);
    }

    public boolean toggleFreeze(UUID uuid) {
        if (frozenPlayers.contains(uuid)) {
            frozenPlayers.remove(uuid);
            return false;
        } else {
            frozenPlayers.add(uuid);
            return true;
        }
    }

    public boolean isFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    public double getViolations(UUID uuid) {
        return violations.getOrDefault(uuid, 0.0);
    }

    public void clearViolations(UUID uuid) {
        violations.remove(uuid);
    }
}
