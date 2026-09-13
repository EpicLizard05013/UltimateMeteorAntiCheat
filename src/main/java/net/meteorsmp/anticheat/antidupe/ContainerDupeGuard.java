package net.meteorsmp.anticheat.antidupe;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashSet;
import java.util.Set;

public class ContainerDupeGuard implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Set<Long> unloadingChunkKeys = new HashSet<>();

    public ContainerDupeGuard(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        long key = (((long) event.getChunk().getX()) << 32) | (event.getChunk().getZ() & 0xFFFFFFFFL);
        unloadingChunkKeys.add(key);

        // Clear record after 2 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> unloadingChunkKeys.remove(key), 40L);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onContainerClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getLocation() == null) {
            return;
        }

        int chunkX = event.getClickedInventory().getLocation().getBlockX() >> 4;
        int chunkZ = event.getClickedInventory().getLocation().getBlockZ() >> 4;
        long key = (((long) chunkX) << 32) | (chunkZ & 0xFFFFFFFFL);

        // Cancel clicks inside containers located in chunks actively unloading
        if (unloadingChunkKeys.contains(key)) {
            event.setCancelled(true);
            plugin.getLogger().warning("[MeteorAC-DupeGuard] Blocked illegal interaction in unloading chunk for " + event.getWhoClicked().getName());
        }
    }
}
