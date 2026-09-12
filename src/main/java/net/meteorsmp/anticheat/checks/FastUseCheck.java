package net.meteorsmp.anticheat.checks;

import net.meteorsmp.anticheat.manager.ConfigManager;
import net.meteorsmp.anticheat.manager.ViolationManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FastUseCheck implements Listener {

    private final ConfigManager cfg;
    private final ViolationManager violations;
    private final Map<UUID, Long> useStarted = new HashMap<>();

    public FastUseCheck(JavaPlugin plugin, ConfigManager cfg, ViolationManager violations) {
        this.cfg = cfg;
        this.violations = violations;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().name().startsWith("RIGHT_CLICK")) return;

        Material type = event.getPlayer().getInventory().getItemInMainHand().getType();
        if (!type.isEdible() && !type.name().contains("POTION")) return;

        useStarted.putIfAbsent(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!cfg.isEnabled("combat.fastuse")) return;

        Player player = event.getPlayer();
        if (player.hasPermission("uac.bypass")) return;

        Long started = useStarted.remove(player.getUniqueId());
        if (started == null) return;

        long elapsed = System.currentTimeMillis() - started;
        if (elapsed > 0 && elapsed < cfg.getInt("combat.fastuse.min-duration-ms", 1200)) {
            violations.addViolation(player, "FastUse", 1);
        }
    }
}
