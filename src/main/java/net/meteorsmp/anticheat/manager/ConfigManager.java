package net.meteorsmp.anticheat.manager;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled(String path) {
        return plugin.getConfig().getBoolean("checks." + path, true);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return plugin.getConfig().getBoolean(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        return plugin.getConfig().getDouble(path, defaultValue);
    }
}
