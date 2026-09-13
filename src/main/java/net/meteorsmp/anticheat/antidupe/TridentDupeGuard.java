package net.meteorsmp.anticheat.antidupe;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRiptideEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TridentDupeGuard implements Listener {

    private final UltimateMeoterAnticheat plugin;
    private final Set<UUID> activeRiptide = new HashSet<>();

    public TridentDupeGuard(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRiptide(PlayerRiptideEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        activeRiptide.add(uuid);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> activeRiptide.remove(uuid), 30L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (activeRiptide.contains(player.getUniqueId()) || player.getInventory().getItemInMainHand().getType() == Material.TRIDENT) {
            player.saveData(); // Force-save player state to prevent state rollback
        }
    }
}
