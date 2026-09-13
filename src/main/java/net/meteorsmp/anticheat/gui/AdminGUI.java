package net.meteorsmp.anticheat.gui;

import net.meteorsmp.anticheat.UltimateMeoterAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class AdminGUI implements Listener {

    private final UltimateMeoterAnticheat plugin;

    public AdminGUI(UltimateMeoterAnticheat plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("gui.title", "&c&lMeteorAC &8| Control Panel"));
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        // Status & Version Button
        int behind = plugin.getVersionsBehind();
        Material updateMat = behind == 0 ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        String updateTitle = behind == 0 ? "&a&lPlugin Up to Date" : "&c&lUpdate Available!";
        List<String> updateLore = behind == 0 
                ? List.of("&7Installed Version: &ev" + plugin.getDescription().getVersion())
                : List.of("&cYou are &e" + behind + " &cupdates behind!", "&7Latest: &ev" + plugin.getLatestVersion(), "&eClick to log details.");
        gui.setItem(10, createItem(updateMat, updateTitle, updateLore));

        // Toggle Alerts
        boolean alertsOn = plugin.getViolationManager().hasAlertsEnabled(player.getUniqueId());
        gui.setItem(12, createItem(
                alertsOn ? Material.BELL : Material.ANVIL,
                "&e&lVerbose Alerts: " + (alertsOn ? "&aENABLED" : "&cDISABLED"),
                List.of("&7Click to toggle real-time violation alerts.")
        ));

        // Reload Plugin
        gui.setItem(14, createItem(Material.NETHER_STAR, "&b&lReload Config", List.of("&7Click to reload config.yml & punishments.yml.")));

        // Clear All Violations
        gui.setItem(16, createItem(Material.MILK_BUCKET, "&d&lClear All VLs", List.of("&7Click to reset active player violation levels.")));

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String expectedTitle = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("gui.title", "&c&lMeteorAC &8| Control Panel"));
        
        if (event.getView().getTitle().equals(expectedTitle)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            switch (clicked.getType()) {
                case BELL, ANVIL -> {
                    boolean nowOn = plugin.getViolationManager().toggleAlerts(player.getUniqueId());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            "&8[&cMeteorAC&8] &7Alerts are now " + (nowOn ? "&aENABLED" : "&cDISABLED")));
                    openGUI(player);
                }
                case NETHER_STAR -> {
                    plugin.reloadConfig();
                    plugin.getPunishmentManager().reloadPunishments();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cMeteorAC&8] &aConfig & Punishments reloaded!"));
                    player.closeInventory();
                }
                case MILK_BUCKET -> {
                    plugin.getViolationManager().clearAllViolations();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&cMeteorAC&8] &aCleared all active player VL records."));
                    player.closeInventory();
                }
            }
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore != null) {
                meta.setLore(lore.stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
