package net.meteorsmp.anticheat.manager;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhookManager {

    private final UltimateMeoterAnticheat plugin;

    public DiscordWebhookManager(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    public void sendAlertEmbed(Player player, String checkName, int violationLevel) {
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL_HERE")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonPayload = "{"
                        + "\"embeds\": [{"
                        + "\"title\": \"🚨 MeteorAC Detection Alert\","
                        + "\"color\": 15158332,"
                        + "\"fields\": ["
                        + "{\"name\": \"Player\", \"value\": \"" + player.getName() + "\", \"inline\": true},"
                        + "{\"name\": \"Check\", \"value\": \"" + checkName + "\", \"inline\": true},"
                        + "{\"name\": \"VL\", \"value\": \"" + violationLevel + "\", \"inline\": true},"
                        + "{\"name\": \"Ping\", \"value\": \"" + player.getPing() + "ms\", \"inline\": true},"
                        + "{\"name\": \"Client Brand\", \"value\": \"" + player.getClientBrandName() + "\", \"inline\": true}"
                        + "]"
                        + "}]"
                        + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                conn.getResponseCode(); // Trigger request
            } catch (Exception e) {
                plugin.getLogger().warning("[MeteorAC] Failed to dispatch Discord alert: " + e.getMessage());
            }
        });
    }
}
