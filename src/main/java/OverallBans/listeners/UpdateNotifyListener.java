package OverallBans.listeners;

import OverallBans.PunishmentPlugin;
import OverallBans.update.UpdateManager;
import OverallBans.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {

    private final PunishmentPlugin plugin;

    public UpdateNotifyListener(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (!player.hasPermission("punishment.editor") && !player.isOp()) return;

        UpdateManager um = plugin.getUpdateManager();
        if (um == null) return;

        if (um.isDisabled()) {
            player.sendMessage(MessageUtils.colorize(um.getDeprecationMessage()));
        } else if (um.isUpdateAvailable()) {
            player.sendMessage(MessageUtils.colorize(um.getUpdateMessage()));
        }
    }
}
