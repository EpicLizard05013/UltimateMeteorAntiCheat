package net.meteorsmp.anticheat.commands;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhitelistCommand implements CommandExecutor {

    private final UltimateMeoterAnticheat plugin;

    public WhitelistCommand(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("meteorsmp.anticheat.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /acwhitelist <add|remove> <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            plugin.getWhitelistManager().addWhitelist(target.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + target.getName() + " added to anticheat bypass whitelist.");
        } else if (args[0].equalsIgnoreCase("remove")) {
            plugin.getWhitelistManager().removeWhitelist(target.getUniqueId());
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " removed from anticheat bypass whitelist.");
        }

        return true;
    }
}
