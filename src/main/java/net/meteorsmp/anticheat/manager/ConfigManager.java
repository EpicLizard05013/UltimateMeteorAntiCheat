package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final UltimateMeoterAnticheat plugin;

    public ConfigManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String getPrefix() {
        return getConfig().getString("prefix", "&8[&cMeteorAC&8] ").replace("&", "§");
    }

    public boolean isWorldBypassed(String checkCategory, String worldName) {
        List<String> bypassedWorlds = getConfig().getStringList("world-bypasses." + checkCategory.toLowerCase());
        if (bypassedWorlds.stream().anyMatch(w -> w.equalsIgnoreCase(worldName))) {
            return true;
        }
        
        String worldMode = getConfig().getString("world-rules.worlds." + worldName + ".mode", "inherit");
        return worldMode.equalsIgnoreCase("disabled") || worldMode.equalsIgnoreCase("ignore");
    }

    public boolean isCheckEnabled(String category, String checkName) {
        return getConfig().getBoolean("checks." + category + "." + checkName + ".enabled", true);
    }

    public boolean isDupeMitigationEnabled(String mitigationKey) {
        return getConfig().getBoolean("antidupe-engine.specific-dupe-mitigations." + mitigationKey + ".enabled", true);
    }

    public boolean isTransactionHandlerEnabled(String handlerKey) {
        return getConfig().getBoolean("transaction-handlers." + handlerKey + ".enabled", true);
    }

    public int getTransactionDelay(String handlerKey) {
        return getConfig().getInt("transaction-handlers." + handlerKey + ".delay-ticks", 1);
    }
}
