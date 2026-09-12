package net.meteorsmp.anticheat.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ACLogger {

    private final JavaPlugin plugin;

    public ACLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void logDupeAttempt(Player player, String reason) {
        plugin.getLogger().warning("[Dupe Prevention] " + player.getName() + " " + reason);
        Bukkit.broadcast("§8[§cMeteorAC§8] §c" + player.getName() + " §7" + reason, "antidupe.notify");
    }

    public void logViolation(String playerName, String checkName, int vl, String details) {
        plugin.getLogger().info("[Violation] " + playerName + " failed " + checkName + " (VL: " + vl + ") - " + details);
    }

    public void logPunishment(String playerName, String punishmentType, String reason) {
        plugin.getLogger().warning("[Punishment] " + playerName + " received " + punishmentType + " for " + reason);
    }

    public void logAction(String actor, String action, String target) {
        plugin.getLogger().info("[Action] " + actor + " performed " + action + " on " + target);
    }
}
