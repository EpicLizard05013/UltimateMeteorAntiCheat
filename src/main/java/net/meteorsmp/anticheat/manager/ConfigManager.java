package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;

public class ConfigManager {

    private final UltimateMeoterAnticheat plugin;

    public ConfigManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "&8[&cMeteorAC&8] ");
    }
}
