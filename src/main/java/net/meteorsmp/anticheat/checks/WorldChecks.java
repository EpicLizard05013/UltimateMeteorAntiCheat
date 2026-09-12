package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldChecks implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Map<UUID, Long> placeHistory = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastInventoryAction = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> transactionDuplicates = new ConcurrentHashMap<>();

    public WorldChecks(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // --- FASTPLACE CHECK ---
        if (plugin.getConfig().getBoolean("checks.world.fastplace", true)) {
            if (placeHistory.containsKey(uuid)) {
                long delay = now - placeHistory.get(uuid);
                if (delay < 40) {
                    event.setCancelled(true);
                    plugin.getViolationManager().flag(player, "World", "FastPlace (Type A)", 1.0);
                }
            }
            placeHistory.put(uuid, now);
        }

        // --- SCAFFOLD CHECK ---
        if (plugin.getConfig().getBoolean("checks.world.scaffold", true)) {
            if (player.getPitch() > 82.0f && player.isSprinting() && event.getBlockAgainst().getY() < player.getLocation().getY()) {
                event.setCancelled(true);
                plugin.getViolationManager().flag(player, "World", "Scaffold (Type A)", 1.5);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDupe(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Prevent container desync/packet duplication exploits (Vulcan-style transaction auditing)
        long lastClick = lastInventoryAction.getOrDefault(uuid, 0L);
        if (now - lastClick < 25) {
            int recentDupes = transactionDuplicates.getOrDefault(uuid, 0) + 1;
            transactionDuplicates.put(uuid, recentDupes);

            if (recentDupes > 5) {
                event.setCancelled(true);
                plugin.getAcLogger().logAction(player.getName(), "DUPE_ATTEMPT_BLOCKED", "Container Transaction Spam");
                plugin.getViolationManager().flag(player, "World", "AntiDupe (Type A)", 10.0);
                player.kickPlayer("§c[MeteorAC] Security Violation: Item Duplication Attempt Detected.");
                return;
            }
        } else {
            transactionDuplicates.put(uuid, 0);
        }
        lastInventoryAction.put(uuid, now);

        // --- VALUABLE ITEM TRANSACTION LIMITER (100k / High Value Cap) ---
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && isHighValueItem(clickedItem)) {
            if (event.getInventory().getType() == InventoryType.PLAYER && event.isShiftClick()) {
                // Prevent bulk shift-click dupes between containers
                if (clickedItem.getAmount() > 64) {
                    event.setCancelled(true);
                    plugin.getViolationManager().flag(player, "World", "AntiDupe (Type B)", 5.0);
                }
            }
        }
    }

    private boolean isHighValueItem(ItemStack item) {
        Material type = item.getType();
        // Capped item check for currency notes, enchanted god items, or high-tier valuables
        return type == Material.NETHER_STAR || type == Material.ENCHANTED_GOLDEN_APPLE || type == Material.BEACON;
    }
}
