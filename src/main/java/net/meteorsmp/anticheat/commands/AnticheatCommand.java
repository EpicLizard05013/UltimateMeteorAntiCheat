package net.meteorsmp.anticheat.commands;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnticheatCommand implements CommandExecutor {

    private final UltimateMeoterAnticheat plugin;

    public AnticheatCommand(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("meteor.admin")) {
            sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lMeteorAC &7Enterprise Edition"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/meteor reload &7- Reload config and punishments"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/meteor alerts &7- Toggle verbose violation alerts"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/meteor logs <player> &7- View recent violations"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/meteor freeze <player> &7- Halt a player's packets"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/meteor scan <player> &7- Deep NBT inventory scan"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/meteor crash <player> &7- Send fatal client packets"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getPunishmentManager().reloadPunishments();
                sender.sendMessage(ChatColor.GREEN + "[MeteorAC] Config and Punishments reloaded successfully.");
            }
            case "alerts" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be executed in-game by a player.");
                    return true;
                }
                boolean enabled = plugin.getViolationManager().toggleAlerts(player.getUniqueId());
                String stateText = enabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED";
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cMeteorAC&8] &7Verbose violation alerts are now " + stateText + "&7."));
            }
            case "logs" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor logs <player>");
                    return true;
                }
                String targetName = args[1];
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cMeteorAC&8] &7Fetching recent violations for &e" + targetName + "&7..."));
                plugin.getViolationManager().displayLogs(sender, targetName);
            }
            case "freeze" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor freeze <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    plugin.getViolationManager().toggleFreeze(target.getUniqueId()); 
                    sender.sendMessage(ChatColor.AQUA + "Toggled freeze state for " + target.getName());
                } else {
                    sender.sendMessage(ChatColor.RED + "Player target not found online.");
                }
            }
            case "scan" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor scan <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    sender.sendMessage(ChatColor.YELLOW + "[MeteorAC] Deep scanning " + target.getName() + " for illegal NBTs...");
                    plugin.getAcLogger().logAction(sender.getName(), "DEEP_SCAN", target.getName());
                } else {
                    sender.sendMessage(ChatColor.RED + "Player target not found online.");
                }
            }
            case "crash" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor crash <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    sender.sendMessage(ChatColor.RED + "[MeteorAC] Dispatching payload to " + target.getName());
                    target.spawnParticle(org.bukkit.Particle.EXPLOSION, target.getLocation(), Integer.MAX_VALUE);
                } else {
                    sender.sendMessage(ChatColor.RED + "Player target not found online.");
                }
            }
            default -> sender.sendMessage(ChatColor.RED + "Unknown argument. Type /meteor for help.");
        }
        return true;
    }
}
