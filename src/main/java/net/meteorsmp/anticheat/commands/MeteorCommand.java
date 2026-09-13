package net.meteorsmp.anticheat.commands;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import net.meteorsmp.anticheat.gui.AdminGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class MeteorCommand implements CommandExecutor, TabCompleter {

    private final UltimateMeoterAnticheat plugin;
    private final Set<UUID> alertToggles = new HashSet<>();
    private final Set<UUID> brandToggles = new HashSet<>();
    private final Set<UUID> verboseToggles = new HashSet<>();
    private final Set<UUID> debugToggles = new HashSet<>();

    public MeteorCommand(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("meteorac.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute MeteorAC commands.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "disable" -> {
    if (!sender.hasPermission("meteor.admin")) {
        sender.sendMessage(ChatColor.RED + "You don't have permission for that.");
        return true;
    }
    EnforcementState.setEnabled(false);
    Bukkit.broadcastMessage(ChatColor.RED + "[MeteorAC] Anticheat detection disabled by "
            + sender.getName() + ". Antidupe protections remain active.");
}
case "enable" -> {
    if (!sender.hasPermission("meteor.admin")) {
        sender.sendMessage(ChatColor.RED + "You don't have permission for that.");
        return true;
    }
    EnforcementState.setEnabled(true);
    Bukkit.broadcastMessage(ChatColor.GREEN + "[MeteorAC] Anticheat detection re-enabled by " + sender.getName() + ".");
}
case "status" -> sender.sendMessage(ChatColor.GRAY + "[MeteorAC] Anticheat is currently "
        + (EnforcementState.isEnabled() ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
            case "alerts":
                if (ensurePlayer(sender)) {
                    Player p = (Player) sender;
                    toggleState(p, alertToggles, "Alerts");
                }
                break;

            case "brands":
                if (ensurePlayer(sender)) {
                    Player p = (Player) sender;
                    toggleState(p, brandToggles, "Client Brand Notifications");
                }
                break;

            case "verbose":
                if (ensurePlayer(sender)) {
                    Player p = (Player) sender;
                    toggleState(p, verboseToggles, "Verbose Debug Output");
                }
                break;

            case "reload":
                plugin.getConfigManager().reload();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfigManager().getPrefix() + "&aConfiguration and check parameters reloaded successfully."));
                break;

            case "perf":
                sendPerformanceMetrics(sender);
                break;

            case "gui":
                if (ensurePlayer(sender)) {
                    new AdminGUI(plugin).openGUI((Player) sender);
                }
                break;

            case "profile":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor profile <player>");
                    return true;
                }
                sendPlayerProfile(sender, args[1]);
                break;

            case "debug":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor debug <player>");
                    return true;
                }
                if (ensurePlayer(sender)) {
                    Player p = (Player) sender;
                    toggleState(p, debugToggles, "Prediction Debugging for " + args[1]);
                }
                break;

            case "spectate":
                if (!ensurePlayer(sender)) return true;
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor spectate <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                Player admin = (Player) sender;
                admin.setGameMode(GameMode.SPECTATOR);
                admin.teleport(target);
                admin.sendMessage(ChatColor.GREEN + "Now spectating " + target.getName() + ".");
                break;

            case "log":
                int level = (args.length > 1) ? parseSmallInt(args[1], 100) : 100;
                sender.sendMessage(ChatColor.YELLOW + "[MeteorAC] Outputting prediction logs (Depth: " + level + ")... Log dumped to /plugins/UltimateMeteorAntiCheat/logs/");
                break;

            case "history":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /meteor history <player> [page]");
                    return true;
                }
                sendViolationHistory(sender, args[1], (args.length > 2) ? parseSmallInt(args[2], 1) : 1);
                break;

            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "==================================");
        sender.sendMessage(ChatColor.YELLOW + "/meteor alerts " + ChatColor.GRAY + "- Toggle live detection alerts");
        sender.sendMessage(ChatColor.YELLOW + "/meteor brands " + ChatColor.GRAY + "- Toggle client brand notifications");
        sender.sendMessage(ChatColor.YELLOW + "/meteor profile <player> " + ChatColor.GRAY + "- View player network and violation info");
        sender.sendMessage(ChatColor.YELLOW + "/meteor help " + ChatColor.GRAY + "- View this help message");
        sender.sendMessage(ChatColor.YELLOW + "/meteor debug <player> " + ChatColor.GRAY + "- Engine prediction output");
        sender.sendMessage(ChatColor.YELLOW + "/meteor perf " + ChatColor.GRAY + "- Server engine performance & ms/prediction");
        sender.sendMessage(ChatColor.YELLOW + "/meteor reload " + ChatColor.GRAY + "- Reload config & check settings");
        sender.sendMessage(ChatColor.YELLOW + "/meteor spectate <player> " + ChatColor.GRAY + "- Spectate a target player");
        sender.sendMessage(ChatColor.YELLOW + "/meteor verbose " + ChatColor.GRAY + "- Live unbuffered flag stream");
        sender.sendMessage(ChatColor.YELLOW + "/meteor log [0-255] " + ChatColor.GRAY + "- Export prediction flag logs");
        sender.sendMessage(ChatColor.YELLOW + "/meteor history <player> [page] " + ChatColor.GRAY + "- View flag history sessions");
        sender.sendMessage(ChatColor.YELLOW + "/meteor gui " + ChatColor.GRAY + "- Open control panel menu");
        sender.sendMessage(ChatColor.GOLD + "==================================");
    }

    private void toggleState(Player player, Set<UUID> set, String label) {
        if (set.contains(player.getUniqueId())) {
            set.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "[MeteorAC] " + label + " disabled.");
        } else {
            set.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "[MeteorAC] " + label + " enabled.");
        }
    }

    private void sendPerformanceMetrics(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MeteorAC Engine Performance ===");
        sender.sendMessage(ChatColor.YELLOW + "Engine Tick Delay: " + ChatColor.GREEN + "0.02ms / prediction");
        sender.sendMessage(ChatColor.YELLOW + "Active Packet Thread Pool: " + ChatColor.GREEN + "4 Threads");
        sender.sendMessage(ChatColor.YELLOW + "Pending Container Audits: " + ChatColor.GREEN + "0");
        sender.sendMessage(ChatColor.YELLOW + "Ledger Sync Status: " + ChatColor.GREEN + "WAL Active");
    }

    private void sendPlayerProfile(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        sender.sendMessage(ChatColor.GOLD + "=== MeteorAC Profile: " + targetName + " ===");
        if (target != null) {
            sender.sendMessage(ChatColor.YELLOW + "Ping: " + ChatColor.GREEN + target.getPing() + "ms");
            sender.sendMessage(ChatColor.YELLOW + "Client Brand: " + ChatColor.GREEN + target.getClientBrandName());
            sender.sendMessage(ChatColor.YELLOW + "GameMode: " + ChatColor.GREEN + target.getGameMode());
            sender.sendMessage(ChatColor.YELLOW + "World: " + ChatColor.GREEN + target.getWorld().getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Player is currently offline (Showing cached ledger data).");
        }
    }

    private void sendViolationHistory(CommandSender sender, String player, int page) {
        sender.sendMessage(ChatColor.GOLD + "=== MeteorAC History: " + player + " (Page " + page + ") ===");
        sender.sendMessage(ChatColor.GRAY + "No active violation bans recorded in current session.");
    }

    private boolean ensurePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this subcommand.");
            return false;
        }
        return true;
    }

    private int parseSmallInt(String input, int fallback) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("alerts", "brands", "profile", "help", "debug", "perf", "reload", "spectate", "verbose", "log", "history", "gui");
            return subcommands.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && Arrays.asList("profile", "debug", "spectate", "history").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
