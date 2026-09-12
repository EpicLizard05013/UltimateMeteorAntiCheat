package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ACLogger {
    private final UltimateMeoterAnticheat plugin;
    private final File logFolder;

    public ACLogger(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
        this.logFolder = new File(plugin.getDataFolder(), "logs");
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }
    }

    // Direct overload to support Player object and simple message from ViolationManager
    public void logViolation(Player player, String details) {
        logViolation(player.getName(), "GENERAL", 1, details);
    }

    public void logViolation(String playerName, String checkName, int vl, String details) {
        writeLog("violations", playerName + " failed " + checkName + " (VL: " + vl + ") | Details: " + details);
    }

    public void logPunishment(String playerName, String action, String reason) {
        writeLog("punishments", "ACTION: " + action + " | TARGET: " + playerName + " | REASON: " + reason);
    }

    // Resolves logAction symbol error in AnticheatCommand
    public void logAction(String actor, String action, String target) {
        writeLog("actions", "ADMIN: " + actor + " | ACTION: " + action + " | TARGET: " + target);
    }

    private void writeLog(String prefix, String message) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        File logFile = new File(logFolder, prefix + "-" + date + ".log");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
                out.println("[" + time + "] " + message);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to write to MeteorAC log file: " + logFile.getName());
            }
        });
    }
}
