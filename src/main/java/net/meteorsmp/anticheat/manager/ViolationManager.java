package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ViolationManager {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Double> violations = new ConcurrentHashMap<>();
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> alertsEnabled = ConcurrentHashMap.newKeySet();
    private final Map<String, List<String>> violationLogs = new ConcurrentHashMap<>();

    public ViolationManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    public void flag(Player player, String category, String checkName, double vl) {
        UUID uuid = player.getUniqueId();
        double currentVl = violations.getOrDefault(uuid, 0.0) + vl;
        violations.put(uuid, currentVl);

        int intVl = (int) Math.round(currentVl);
        
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String logEntry = "&8[&e" + timeStamp + "&8] &f" + category + "&7/" + checkName + " &cVL: " + intVl + " &7(+" + vl + ")";
        violationLogs.computeIfAbsent(player.getName().toLowerCase(), k -> new CopyOnWriteArrayList<>()).add(logEntry);

        broadcastAlert(player, category, checkName, intVl, vl);

        plugin.getAcLogger().logViolation(player.getName(), category + ":" + checkName, intVl, "+" + vl + " VL");
        
        plugin.getPunishmentManager().executePunishment(player.getName(), category.toLowerCase(), intVl);

        if (currentVl >= 50.0) {
            plugin.getPunishmentManager().punish(player, "Excessive " + category + " flags (" + checkName + ")");
        }
    }

    private void broadcastAlert(Player player, String category, String checkName, int totalVl, double addedVl) {
        String alertMessage = ChatColor.translateAlternateColorCodes('&',
                "&8[&cMeteorAC&8] &e" + player.getName() + " &7failed &f" + category + "/" + checkName +
                        " &7[&cVL: " + totalVl + " &7(&c+" + addedVl + "&7)]"
        );

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("meteor.admin") && alertsEnabled.contains(online.getUniqueId())) {
                online.sendMessage(alertMessage);
            }
        }
    }

    public boolean toggleAlerts(UUID uuid) {
        if (alertsEnabled.contains(uuid)) {
            alertsEnabled.remove(uuid);
            return false;
        } else {
            alertsEnabled.add(uuid);
            return true;
        }
    }

    public boolean hasAlertsEnabled(UUID uuid) {
        return alertsEnabled.contains(uuid);
    }

    public void displayLogs(CommandSender sender, String targetName) {
        List<String> logs = violationLogs.get(targetName.toLowerCase());

        if (logs == null || logs.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cMeteorAC&8] &7No violation logs found for &e" + targetName + "&7."));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lMeteorAC Logs &7- &e" + targetName));
        
        int start = Math.max(0, logs.size() - 10);
        for (int i = start; i < logs.size(); i++) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', logs.get(i)));
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
    }

    public void addViolation(Player player, String check, int vl) {
        if (!EnforcementState.isEnabled()) return 0;
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

    public void clearAllViolations() {
        violations.clear();
        violationLogs.clear();
    }
}
