package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PunishmentManager {
    private final UltimateMeoterAnticheat plugin;
    private FileConfiguration punishmentsConfig;
    private File punishmentsFile;

    public PunishmentManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
        createPunishmentsFile();
    }

    public void createPunishmentsFile() {
        punishmentsFile = new File(plugin.getDataFolder(), "punishments.yml");
        if (!punishmentsFile.exists()) {
            punishmentsFile.getParentFile().mkdirs();
            plugin.saveResource("punishments.yml", false);
        }
        punishmentsConfig = YamlConfiguration.loadConfiguration(punishmentsFile);
    }

    public void reloadPunishments() {
        punishmentsConfig = YamlConfiguration.loadConfiguration(punishmentsFile);
    }

    public void executePunishment(String playerName, String category, int vl) {
        String commandPath = "escalation-ladder." + category + "." + vl;
        if (punishmentsConfig.contains(commandPath)) {
            String command = punishmentsConfig.getString(commandPath).replace("%player%", playerName);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                plugin.getAcLogger().logPunishment(playerName, "AUTO-ESCALATION", "Reached VL " + vl + " for " + category);
            });
        }
    }
}
