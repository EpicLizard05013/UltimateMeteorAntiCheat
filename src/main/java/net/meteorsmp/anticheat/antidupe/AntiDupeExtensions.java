package net.meteorsmp.anticheat.antidupe;

import net.meteorsmp.anticheat.manager.ACLogger;
import net.meteorsmp.anticheat.manager.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiDupeExtensions implements Listener {

    private final ConfigManager cfg;
    private final ACLogger logger;
    private final Map<UUID, Long> lastTrade = new HashMap<>();

    public AntiDupeExtensions(JavaPlugin plugin, ConfigManager cfg, ACLogger logger) {
        this.cfg = cfg;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.hasPermission("uac.bypass")) return;

        blockBundleInBundle(event, player);
        limitVillagerTrades(event, player);
    }

    private void blockBundleInBundle(InventoryClickEvent event, Player player) {
        if (!cfg.getBoolean("antidupe.block-bundle-in-bundle", true)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor != null && cursor.getType() == Material.BUNDLE
                && current != null && current.getType() == Material.BUNDLE) {
            event.setCancelled(true);
            logger.logDupeAttempt(player, "tried to place a bundle inside another bundle");
        }
    }

    private void limitVillagerTrades(InventoryClickEvent event, Player player) {
        if (!cfg.getBoolean("antidupe.villager-trade-cooldown-enabled", true)) return;
        if (event.getInventory().getType() != InventoryType.MERCHANT) return;
        if (event.getRawSlot() != 2) return;

        long now = System.currentTimeMillis();
        long cooldown = cfg.getInt("antidupe.villager-trade-cooldown-ms", 150);

        Long last = lastTrade.put(player.getUniqueId(), now);
        if (last != null && now - last < cooldown) {
            event.setCancelled(true);
            logger.logDupeAttempt(player, "villager trade completed below the minimum interval");
        }
    }
}
