package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ACLogger {

    private final UltimateMeoterAnticheat plugin;

    public ACLogger(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    public void logViolation(Player player, String message) {
        String formatted = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfigManager().getPrefix() + "&e" + player.getName() + " &7failed &f" + message);
        
        notifyAdmins(formatted);
    }

    public void notifyAdmins(String formattedMessage) {
        String msg = ChatColor.translateAlternateColorCodes('&', formattedMessage);
        Bukkit.getConsoleSender().sendMessage(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("meteorsmp.anticheat.admin")) {
                p.sendMessage(msg);
            }
        }
    }
}
