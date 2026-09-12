package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PunishmentManager {

    private final UltimateMeoterAnticheat plugin;

    public PunishmentManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    public void punish(Player player, String reason) {
        String action = plugin.getConfig().getString("punishments.action", "NOTIFY").toUpperCase();
        String kickMsg = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("punishments.kick-message", "&cFailed packet validation check."));

        Bukkit.getScheduler().runTask(plugin, () -> {
            switch (action) {
                case "KICK" -> player.kickPlayer(kickMsg);
                case "BAN" -> Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), reason, null, "MeteorAC");
                case "NOTIFY" -> plugin.getAcLogger().notifyAdmins("&c[FLAG HIGH] " + player.getName() + " reached threshold for " + reason);
            }
        });
    }
}
