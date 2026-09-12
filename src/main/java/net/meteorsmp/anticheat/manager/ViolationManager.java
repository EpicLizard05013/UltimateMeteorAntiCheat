package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ConcurrentHashMap;
import java.util.UUID;

public class ViolationManager {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Integer> violations = new ConcurrentHashMap<>();
    private final Set<UUID> frozenPlayers = new HashSet<>();

    public ViolationManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    public void addViolation(Player player, String check, int vl) {
        UUID uuid = player.getUniqueId();
        int currentVl = violations.getOrDefault(uuid, 0) + vl;
        violations.put(uuid, currentVl);

        plugin.getAcLogger().logViolation(player, check + " (VL: " + currentVl + ")");
        
        if (currentVl >= 20) {
            plugin.getPunishmentManager().punish(player, "Excessive " + check + " violations.");
        }
    }

    // Resolves toggleFreeze symbol error in AnticheatCommand
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

    public int getViolations(UUID uuid) {
        return violations.getOrDefault(uuid, 0);
    }

    public void clearViolations(UUID uuid) {
        violations.remove(uuid);
    }
}
