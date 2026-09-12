package net.meteorsmp.anticheat.antidupe;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.ShulkerBox;

public class AntiDupeListener implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public AntiDupeListener(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.getConfig().getBoolean("antidupe.enabled", true)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (plugin.getConfig().getBoolean("antidupe.block-shulker-in-shulker", true)) {
            if (current != null && isShulker(current.getType()) && cursor != null && isShulker(cursor.getType())) {
                if (event.getInventory().getHolder() instanceof ShulkerBox) {
                    event.setCancelled(true);
                    player.sendMessage("§cNested shulkers are restricted on this server.");
                }
            }
        }
    }

    private boolean isShulker(Material material) {
        return material.name().contains("SHULKER_BOX");
    }
}
