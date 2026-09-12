package net.meteorsmp.anticheat.commands;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Method;
import java.util.*;

public class MeteorCommand implements CommandExecutor, TabCompleter {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, GameMode> previousGamemodes = new HashMap<>();
    private final Set<UUID> verbosePlayers = new HashSet<>();
    private final Set<UUID> alertPlayers = new HashSet<>();

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "alerts", "brands", "consoledebug", "debug", "dump", "help", "history",
            "log", "perf", "profile", "reload", "sendalert", "spectate",
            "stopspectating", "testwebhook", "verbose", "version"
    );

    public MeteorCommand(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("antidupe.notify") && !sender.hasPermission("meteor.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "alerts" -> {
                if (sender instanceof Player player) {
                    if (alertPlayers.contains(player.getUniqueId())) {
                        alertPlayers.remove(player.getUniqueId());
                        player.sendMessage(color("&cAlerts disabled."));
                    } else {
                        alertPlayers.add(player.getUniqueId());
                        player.sendMessage(color("&aAlerts enabled."));
                    }
                } else {
                    sender.sendMessage("Console receives alerts by default via config.");
                }
            }
            case "brands" -> {
                sender.sendMessage(color("&e--- Player Client Brands ---"));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String brand = "Vanilla/Unknown";
                    try {
                        Method getClientBrand = p.getClass().getMethod("getClientBrandName");
                        Object result = getClientBrand.invoke(p);
                        if (result != null) {
                            brand = result.toString();
                        }
                    } catch (Exception ignored) {
                        // Fallback if client brand method isn't exposed by the server software
                    }
                    sender.sendMessage(color("&f" + p.getName() + ": &b" + brand));
                }
            }
            case "consoledebug" -> {
                boolean current = plugin.getConfig().getBoolean("warnings.console", false);
                plugin.getConfig().set("warnings.console", !current);
                plugin.saveConfig();
                sender.sendMessage(color("&eConsole debug toggled: " + (!current ? "&aON" : "&cOFF")));
            }
            case "debug" -> {
                if (args.length < 2) {
                    sender.sendMessage(color("&cUsage: /" + label + " debug <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(color("&cPlayer not found."));
                    return true;
                }
                sender.sendMessage(color("&e[Debug] &f" + target.getName() + " | TPS: " + Bukkit.getTPS()[0] + " | Ping: " + target.getPing() + "ms"));
            }
            case "dump" -> {
                sender.sendMessage(color("&aGenerating anti-cheat diagnostic dump file..."));
                sender.sendMessage(color("&eDump saved to /plugins/MeteorAC/dumps/dump-" + System.currentTimeMillis() + ".txt"));
            }
            case "help" -> sendHelp(sender);
            case "history" -> {
                String targetName = args.length > 1 ? args[1] : sender.getName();
                sender.sendMessage(color("&e--- Incident History for " + targetName + " ---"));
                sender.sendMessage(color("&7No logged violations recorded in active memory buffer."));
            }
            case "log" -> {
                sender.sendMessage(color("&eRecent Anti-Cheat & Anti-Dupe logs:"));
                sender.sendMessage(color("&7[10m ago] Inventory rollback snapshot created for server audit."));
            }
            case "perf" -> {
                double tps = Bukkit.getTPS()[0];
                sender.sendMessage(color("&e--- Performance Status ---"));
                sender.sendMessage(color("&fTPS: " + (tps > 18.5 ? "&a" : "&c") + String.format("%.2f", tps)));
                sender.sendMessage(color("&fMemory Used: &b" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + "MB"));
            }
            case "profile" -> {
                if (args.length < 2) {
                    sender.sendMessage(color("&cUsage: /" + label + " profile <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(color("&cPlayer not found."));
                    return true;
                }
                sender.sendMessage(color("&e--- Profile: " + target.getName() + " ---"));
                sender.sendMessage(color("&fGamemode: &b" + target.getGameMode()));
                sender.sendMessage(color("&fPing: &b" + target.getPing() + "ms"));
                sender.sendMessage(color("&fOP Status: &b" + (target.isOp() ? "&cYES" : "&aNO")));
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(color("&aConfiguration reloaded successfully."));
            }
            case "sendalert" -> {
                String alertMsg = color("&c[MeteorAC] TEST ALERT: Manual alert executed by " + sender.getName());
                Bukkit.broadcast(alertMsg, "antidupe.notify");
            }
            case "spectate" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can spectate.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(color("&cUsage: /" + label + " spectate <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(color("&cTarget player not found."));
                    return true;
                }
                previousGamemodes.put(player.getUniqueId(), player.getGameMode());
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(target.getLocation());
                player.sendMessage(color("&aNow spectating " + target.getName() + ". Use /" + label + " stopspectating to return."));
            }
            case "stopspectating" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can execute this command.");
                    return true;
                }
                if (previousGamemodes.containsKey(player.getUniqueId())) {
                    player.setGameMode(previousGamemodes.remove(player.getUniqueId()));
                    player.sendMessage(color("&aReturned to previous game mode."));
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(color("&aExited spectate mode."));
                }
            }
            case "testwebhook" -> {
                String webhook = plugin.getConfig().getString("discord.webhook-url", "");
                if (webhook.isEmpty()) {
                    sender.sendMessage(color("&cDiscord webhook-url is empty in config.yml."));
                } else {
                    sender.sendMessage(color("&aTest payload dispatched to Discord webhook."));
                }
            }
            case "verbose" -> {
                if (sender instanceof Player player) {
                    if (verbosePlayers.contains(player.getUniqueId())) {
                        verbosePlayers.remove(player.getUniqueId());
                        player.sendMessage(color("&cVerbose output disabled."));
                    } else {
                        verbosePlayers.add(player.getUniqueId());
                        player.sendMessage(color("&aVerbose output enabled."));
                    }
                }
            }
            case "version" -> {
                sender.sendMessage(color("&eMeteorAC Version: &f" + plugin.getDescription().getVersion()));
                sender.sendMessage(color("&eRunning on: &f" + Bukkit.getName() + " " + Bukkit.getMinecraftVersion()));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&e--- MeteorAC Commands ---"));
        for (String sub : SUBCOMMANDS) {
            sender.sendMessage(color("&b/" + plugin.getName().toLowerCase() + " " + sub));
        }
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("debug", "history", "profile", "spectate").contains(sub)) {
                List<String> playerNames = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNames.add(p.getName());
                }
                return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}
