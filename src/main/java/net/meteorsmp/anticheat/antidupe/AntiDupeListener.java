package net.meteorsmp.anticheat.antidupe;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiDupeListener implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Long> lastCraftTimes = new HashMap<>();

    public AntiDupeListener(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    private boolean isBypassed(Player player, String category) {
        if (player == null) return false;
        if (player.hasPermission("meteorac.bypass.antidupe")) return true;
        return plugin.getConfigManager().isWorldBypassed(category, player.getWorld().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortalTransition(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (isBypassed(player, "antidupe")) return;

        if (plugin.getConfigManager().isDupeMitigationEnabled("portal-dupe")) {
            boolean closeInv = plugin.getConfigManager().getConfig().getBoolean("antidupe-engine.specific-dupe-mitigations.portal-dupe.close-inventory-on-portal-enter", true);
            if (closeInv) {
                player.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortalItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isBypassed(player, "antidupe")) return;

        if (plugin.getConfigManager().isDupeMitigationEnabled("portal-dupe")) {
            if (player.getPortalCooldown() > 0 || player.isInsideVehicle()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMountInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (isBypassed(player, "antidupe")) return;

        if (plugin.getConfigManager().isDupeMitigationEnabled("donkey-chest-dupe")) {
            if (event.getInventory().getHolder() instanceof AbstractHorse) {
                AbstractHorse mount = (AbstractHorse) event.getInventory().getHolder();
                if (mount.isDead() || !mount.isValid()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMountDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.isInsideVehicle() && player.getVehicle() instanceof AbstractHorse) {
            if (plugin.getConfigManager().isDupeMitigationEnabled("donkey-chest-dupe")) {
                player.getVehicle().eject();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBookEdit(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        if (isBypassed(player, "antidupe")) return;

        if (plugin.getConfigManager().isDupeMitigationEnabled("book-and-quill-dupe")) {
            BookMeta newMeta = event.getNewBookMeta();
            int maxPages = plugin.getConfigManager().getConfig().getInt("antidupe-engine.specific-dupe-mitigations.book-and-quill-dupe.max-book-pages", 50);
            int maxBytes = plugin.getConfigManager().getConfig().getInt("antidupe-engine.specific-dupe-mitigations.book-and-quill-dupe.max-bytes-per-page", 256);

            if (newMeta.getPageCount() > maxPages) {
                event.setCancelled(true);
                return;
            }

            for (String page : newMeta.getPages()) {
                if (page.getBytes().length > maxBytes) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFastCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isBypassed(player, "antidupe")) return;

        if (plugin.getConfigManager().isTransactionHandlerEnabled("crafting")) {
            int maxCps = plugin.getConfigManager().getConfig().getInt("transaction-handlers.crafting.max-crafts-per-second", 12);
            long now = System.currentTimeMillis();
            long last = lastCraftTimes.getOrDefault(player.getUniqueId(), 0L);

            if (now - last < (1000 / maxCps)) {
                event.setCancelled(true);
                return;
            }
            lastCraftTimes.put(player.getUniqueId(), now);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIllegalSlotClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isBypassed(player, "antidupe")) return;

        if (event.getSlot() < -1) {
            event.setCancelled(true);
        }
    }
}
