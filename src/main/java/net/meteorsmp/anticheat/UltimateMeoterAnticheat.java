package net.meteorsmp.anticheat;

import net.meteorsmp.anticheat.antidupe.AntiDupeListener;
import net.meteorsmp.anticheat.checks.CombatChecks;
import net.meteorsmp.anticheat.checks.MovementChecks;
import net.meteorsmp.anticheat.checks.WorldChecks;
import net.meteorsmp.anticheat.commands.AnticheatCommand;
import net.meteorsmp.anticheat.commands.WhitelistCommand;
import net.meteorsmp.anticheat.listeners.PacketDesyncListener;
import net.meteorsmp.anticheat.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UltimateMeoterAnticheat extends JavaPlugin implements Listener {

    private static UltimateMeoterAnticheat instance;
    private ConfigManager configManager;
    private ViolationManager violationManager;
    private PunishmentManager punishmentManager;
    private ACLogger acLogger;
    private WhitelistManager whitelistManager;

    private boolean updateAvailable = false;
    private String latestVersion = "";

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Managers
        this.configManager = new ConfigManager(this);
        this.acLogger = new ACLogger(this);
        this.violationManager = new ViolationManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.whitelistManager = new WhitelistManager(this);

        // Check Listeners
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new CombatChecks(this), this);
        getServer().getPluginManager().registerEvents(new MovementChecks(this), this);
        getServer().getPluginManager().registerEvents(new WorldChecks(this), this);
        getServer().getPluginManager().registerEvents(new AntiDupeListener(this), this);
        getServer().getPluginManager().registerEvents(new PacketDesyncListener(), this);

        // Commands
        if (getCommand("meteor") != null) {
            getCommand("meteor").setExecutor(new AnticheatCommand(this));
        } else if (getCommand("anticheat") != null) {
            getCommand("anticheat").setExecutor(new AnticheatCommand(this));
        }

        if (getCommand("acwhitelist") != null) {
            getCommand("acwhitelist").setExecutor(new WhitelistCommand(this));
        }

        // Run Update Check Asynchronously
        checkForUpdates();

        getLogger().info("UltimateMeteorAnticheat (Paper 1.21.11) initialized successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("UltimateMeteorAnticheat disabled.");
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/EpicLizard05013/UltimateMeteorAntiCheat/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "MeteorAC-UpdateChecker");

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String body = response.toString();
                    int tagIndex = body.indexOf("\"tag_name\":\"");
                    if (tagIndex != -1) {
                        int start = tagIndex + 12;
                        int end = body.indexOf("\"", start);
                        latestVersion = body.substring(start, end).replace("v", "");
                        String currentVersion = getDescription().getVersion().replace("v", "");

                        if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                            updateAvailable = true;
                            getLogger().warning("[MeteorAC] An update is available! Current: v" + currentVersion + " | Latest: v" + latestVersion);
                            getLogger().warning("[MeteorAC] Download it at: https://github.com/EpicLizard05013/UltimateMeteorAntiCheat/releases");
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().warning("[MeteorAC] Could not check GitHub for updates: " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (updateAvailable && player.hasPermission("meteor.admin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cMeteorAC&8] &aA new update is available! &7(v" + latestVersion + ")"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cMeteorAC&8] &7Download: &ehttps://github.com/EpicLizard05013/UltimateMeteorAntiCheat/releases"));
        }
    }

    public static UltimateMeoterAnticheat getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public ViolationManager getViolationManager() { return violationManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public ACLogger getAcLogger() { return acLogger; }
    public WhitelistManager getWhitelistManager() { return whitelistManager; }
}
