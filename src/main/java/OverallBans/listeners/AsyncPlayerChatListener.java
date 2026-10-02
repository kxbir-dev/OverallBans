package OverallBans.listeners;

import OverallBans.PunishmentPlugin;
import OverallBans.managers.MuteManager;
import OverallBans.models.PunishmentRecord;
import OverallBans.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Map;

public class AsyncPlayerChatListener implements Listener {

    private final PunishmentPlugin plugin;

    public AsyncPlayerChatListener(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.isDisabled()) return;
        if (event.getPlayer() == null) return;
        String name = event.getPlayer().getName();
        String ip = event.getPlayer().getAddress() == null ? null
                : event.getPlayer().getAddress().getAddress().getHostAddress();

        MuteManager mm = plugin.getMuteManager();
        PunishmentRecord mute = mm.getActiveMute(name, event.getPlayer().getUniqueId(), ip);

        if (mute != null) {
            event.setCancelled(true);

            Map<String, String> ph = plugin.buildPlaceholders(mute);
            ph.put("player", name);

            String variant = mute.getVariant();

            List<String> muteLines = plugin.getConfigManager().getStringList(
                    "mute-messages." + mute.getType().getKey() + "." + variant);
            if (muteLines.isEmpty()) {
                muteLines = plugin.getConfigManager().getStringList(
                        "mute-messages.mute." + variant);
            }
            if (muteLines.isEmpty()) {
                muteLines = plugin.getConfigManager().getStringList(
                        "mute-messages." + mute.getType().getKey() + ".both");
            }
            if (muteLines.isEmpty()) {
                muteLines = plugin.getConfigManager().getStringList("mute-messages.mute.both");
            }
            for (String line : muteLines) {
                event.getPlayer().sendMessage(MessageUtils.format(line, ph));
            }
            return;
        }

        boolean flagged = plugin.getAntiSpamManager().recordMessage(event.getPlayer());
        if (flagged) {
            event.setCancelled(true);
        }
    }
}
