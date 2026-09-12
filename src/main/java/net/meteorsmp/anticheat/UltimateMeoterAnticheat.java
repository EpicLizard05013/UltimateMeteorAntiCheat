package net.meteorsmp.anticheat;

import net.meteorsmp.anticheat.antidupe.AntiDupeListener;
import net.meteorsmp.anticheat.checks.CombatChecks;
import net.meteorsmp.anticheat.checks.MovementChecks;
import net.meteorsmp.anticheat.checks.WorldChecks;
import net.meteorsmp.anticheat.commands.AnticheatCommand;
import net.meteorsmp.anticheat.commands.WhitelistCommand;
import net.meteorsmp.anticheat.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltimateMeoterAnticheat extends JavaPlugin {

    private static UltimateMeoterAnticheat instance;
    private ConfigManager configManager;
    private ViolationManager violationManager;
    private PunishmentManager punishmentManager;
    private ACLogger acLogger;
    private WhitelistManager whitelistManager;

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
        getServer().getPluginManager().registerEvents(new CombatChecks(this), this);
        getServer().getPluginManager().registerEvents(new MovementChecks(this), this);
        getServer().getPluginManager().registerEvents(new WorldChecks(this), this);
        getServer().getPluginManager().registerEvents(new AntiDupeListener(this), this);

        // Commands
        if (getCommand("anticheat") != null) {
            getCommand("anticheat").setExecutor(new AnticheatCommand(this));
        }
        if (getCommand("acwhitelist") != null) {
            getCommand("acwhitelist").setExecutor(new WhitelistCommand(this));
        }

        getLogger().info("UltimateMeteorAnticheat (Paper 1.21.11) initialized successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("UltimateMeteorAnticheat disabled.");
    }

    public static UltimateMeoterAnticheat getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public ViolationManager getViolationManager() { return violationManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public ACLogger getAcLogger() { return acLogger; }
    public WhitelistManager getWhitelistManager() { return whitelistManager; }
}
