package OverallBans.listeners;

import OverallBans.PunishmentPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final PunishmentPlugin plugin;

    public PlayerQuitListener(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer() != null) {
            plugin.getAntiSpamManager().clearPlayer(event.getPlayer().getUniqueId());
        }
    }
}
