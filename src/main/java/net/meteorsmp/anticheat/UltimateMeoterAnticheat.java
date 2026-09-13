package net.meteorsmp.anticheat;

import net.meteorsmp.anticheat.antidupe.AntiDupeListener;
import net.meteorsmp.anticheat.checks.*;
import net.meteorsmp.anticheat.commands.*;
import net.meteorsmp.anticheat.gui.AdminGUI;
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
    private AdminGUI adminGUI;

    private int versionsBehind = 0;
    private String latestVersion = "";

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.acLogger = new ACLogger(this);
        this.violationManager = new ViolationManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.whitelistManager = new WhitelistManager(this);
        this.adminGUI = new AdminGUI(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(adminGUI, this);
        getServer().getPluginManager().registerEvents(new CombatChecks(this), this);
        getServer().getPluginManager().registerEvents(new MovementChecks(this), this);
        getServer().getPluginManager().registerEvents(new WorldChecks(this), this);
        getServer().getPluginManager().registerEvents(new AntiDupeListener(this), this);
        getServer().getPluginManager().registerEvents(new PacketDesyncListener(), this);

        // Register new checks & dupe listeners
        getServer().getPluginManager().registerEvents(new AutoClickerCheck(this), this);
        getServer().getPluginManager().registerEvents(new ContainerDupeGuard(this), this);

        // Register Movement & Combat Checks
        getServer().getPluginManager().registerEvents(new VelocityCheck(this), this);
        getServer().getPluginManager().registerEvents(new ReachCheck(this), this);

        // Register Dupe & Sanitizer Guards
        getServer().getPluginManager().registerEvents(new TridentDupeGuard(this), this);
        getServer().getPluginManager().registerEvents(new ItemSanitizerGuard(this), this);

        // Initialize Discord Webhook Manager
        DiscordWebhookManager webhookManager = new DiscordWebhookManager(this);

        // Register Commands
        if (getCommand("meteor") != null) {
            AnticheatCommand meteorCmd = new AnticheatCommand(this);
            getCommand("meteor").setExecutor(meteorCmd);
            getCommand("meteor").setTabCompleter(meteorCmd);
        }

        if (getConfig().getBoolean("updates.check-on-startup", true)) {
            checkForUpdates();
        }

        getLogger().info("UltimateMeteorAnticheat initialized successfully.");
    }

    private void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                String repo = getConfig().getString("updates.repository", "EpicLizard05013/UltimateMeteorAntiCheat");
                URL url = new URL("https://api.github.com/repos/" + repo + "/releases");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "MeteorAC-UpdateChecker");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) json.append(line);
                    reader.close();

                    String currentVer = getDescription().getVersion().replace("v", "").trim();
                    String[] releases = json.toString().split("\"tag_name\":\"");

                    if (releases.length > 1) {
                        latestVersion = releases[1].split("\"")[0].replace("v", "").trim();

                        int behindCount = 0;
                        for (int i = 1; i < releases.length; i++) {
                            String tag = releases[i].split("\"")[0].replace("v", "").trim();
                            if (tag.equalsIgnoreCase(currentVer)) {
                                break;
                            }
                            behindCount++;
                        }

                        if (!currentVer.equalsIgnoreCase(latestVersion)) {
                            versionsBehind = Math.max(1, behindCount);
                            getLogger().warning("[MeteorAC] You are " + versionsBehind + " update(s) behind!");
                            getLogger().warning("[MeteorAC] Installed: v" + currentVer + " | Latest: v" + latestVersion);
                            getLogger().warning("[MeteorAC] Download latest: https://github.com/" + repo);
                        } else {
                            versionsBehind = 0;
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().warning("[MeteorAC] Could not verify latest version from GitHub: " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onAdminJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (versionsBehind > 0 && player.hasPermission(getConfig().getString("settings.permission-admin", "meteorac.admin"))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&cMeteorAC&8] &cYou are &e" + versionsBehind + " &cupdate(s) behind! &7(Current: &ev" + getDescription().getVersion() + " &7| Latest: &ev" + latestVersion + "&7)"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&cMeteorAC&8] &7Download latest build: &bhttps://github.com/" + getConfig().getString("updates.repository", "EpicLizard05013/UltimateMeteorAntiCheat")));
        }
    }

    public static UltimateMeoterAnticheat getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public ViolationManager getViolationManager() { return violationManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public ACLogger getAcLogger() { return acLogger; }
    public WhitelistManager getWhitelistManager() { return whitelistManager; }
    public AdminGUI getAdminGUI() { return adminGUI; }
    public int getVersionsBehind() { return versionsBehind; }
    public String getLatestVersion() { return latestVersion; }
}
