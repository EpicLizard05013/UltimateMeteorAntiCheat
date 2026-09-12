package net.meteorsmp.anticheat.commands;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AnticheatCommand implements CommandExecutor {

    private final UltimateMeoterAnticheat plugin;

    public AnticheatCommand(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("meteorsmp.anticheat.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            sender.sendMessage(ChatColor.GREEN + "UltimateMeteorAnticheat configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "--- UltimateMeteorAnticheat 1.21.11 ---");
        sender.sendMessage(ChatColor.YELLOW + "/ac reload - Reload settings");
        return true;
    }
}
