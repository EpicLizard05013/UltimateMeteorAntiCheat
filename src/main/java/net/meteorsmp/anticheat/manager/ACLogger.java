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
}
