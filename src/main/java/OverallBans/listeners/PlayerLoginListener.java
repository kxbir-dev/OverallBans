package OverallBans.listeners;

import OverallBans.PunishmentPlugin;
import OverallBans.managers.BanManager;
import OverallBans.models.PunishmentRecord;
import OverallBans.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class PlayerLoginListener implements Listener {

    private final PunishmentPlugin plugin;

    public PlayerLoginListener(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (plugin.isDisabled()) return;
        String name = event.getName();
        String ip = event.getAddress() == null ? null : event.getAddress().getHostAddress();
        String ipSubnet = getSubnet(ip);
        String ipNormalized = normalizeIp(ip);

        BanManager bm = plugin.getBanManager();
        PunishmentRecord ban = bm.getActiveBan(name, null, ip);

        if (ban == null && ip != null) {
            ban = bm.getActiveBan(name, null, ipNormalized);
        }
        if (ban == null && ip != null) {
            ban = bm.getActiveBan(name, null, ipSubnet);
        }
        if (ban == null && ip != null) {
            for (String variant : generateIpVariants(ip)) {
                ban = bm.getActiveBan(name, null, variant);
                if (ban != null) break;
            }
        }

        if (ban == null) return;

        Map<String, String> ph = plugin.buildPlaceholders(ban);
        ph.put("player", name);

        List<String> screenLines = resolveBanScreen(ban);
        List<String> formatted = MessageUtils.formatAll(screenLines, ph);
        String kickMessage = MessageUtils.joinLines(formatted);

        event.setKickMessage(kickMessage);
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, kickMessage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLogin(PlayerLoginEvent event) {
        String name = event.getPlayer().getName();
        InetAddress addr = event.getAddress();
        String ip = addr == null ? null : addr.getHostAddress();
        String ipSubnet = getSubnet(ip);
        String ipNormalized = normalizeIp(ip);

        BanManager bm = plugin.getBanManager();
        PunishmentRecord ban = bm.getActiveBan(name, event.getPlayer().getUniqueId(), ip);

        if (ban == null && ip != null) {
            ban = bm.getActiveBan(name, event.getPlayer().getUniqueId(), ipNormalized);
        }
        if (ban == null && ip != null) {
            ban = bm.getActiveBan(name, event.getPlayer().getUniqueId(), ipSubnet);
        }
        if (ban == null && ip != null) {
            for (String variant : generateIpVariants(ip)) {
                ban = bm.getActiveBan(name, event.getPlayer().getUniqueId(), variant);
                if (ban != null) break;
            }
        }

        if (ban == null) return;

        Map<String, String> ph = plugin.buildPlaceholders(ban);
        ph.put("player", name);

        List<String> screenLines = resolveBanScreen(ban);
        List<String> formatted = MessageUtils.formatAll(screenLines, ph);
        String kickMessage = MessageUtils.joinLines(formatted);

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage);
    }

    private List<String> resolveBanScreen(PunishmentRecord ban) {
        String variant = ban.getVariant();

        List<String> lines = plugin.getConfigManager().getStringList(
                "ban-screens." + ban.getType().getKey() + "." + variant);
        if (lines.isEmpty()) {
            lines = plugin.getConfigManager().getStringList(
                    "ban-screens.ban." + variant);
        }
        if (lines.isEmpty()) {
            lines = plugin.getConfigManager().getStringList(
                    "ban-screens." + ban.getType().getKey() + ".both");
        }
        if (lines.isEmpty()) {
            lines = plugin.getConfigManager().getStringList("ban-screens.ban.both");
        }
        return lines;
    }

    private String normalizeIp(String ip) {
        if (ip == null) return null;
        String trimmed = ip.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.contains(":")) {
            int idx = trimmed.indexOf('%');
            if (idx > 0) trimmed = trimmed.substring(0, idx);
        }
        return trimmed;
    }

    private String getSubnet(String ip) {
        if (ip == null) return null;
        String normalized = normalizeIp(ip);
        if (normalized == null) return null;
        if (normalized.contains(":")) {
            int idx = normalized.indexOf(':');
            if (idx > 0) {
                return normalized.substring(0, idx) + "::";
            }
        }
        if (normalized.contains(".")) {
            String[] parts = normalized.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".*";
            }
        }
        return normalized;
    }

    private List<String> generateIpVariants(String ip) {
        List<String> variants = new java.util.ArrayList<>();
        if (ip == null) return variants;
        String normalized = normalizeIp(ip);
        if (normalized != null) {
            variants.add(normalized);
            if (!normalized.equals(ip)) {
                variants.add(ip);
            }
        }
        String subnet = getSubnet(ip);
        if (subnet != null && !subnet.equals(normalized)) {
            variants.add(subnet);
        }
        if (normalized != null && normalized.contains(":")) {
            String full = normalized;
            String expanded = expandIpv6(full);
            if (expanded != null && !expanded.equals(full)) {
                variants.add(expanded);
            }
            String compressed = compressIpv6(full);
            if (compressed != null && !compressed.equals(full)) {
                variants.add(compressed);
            }
        }
        if (normalized != null && normalized.contains(".")) {
            String[] parts = normalized.split("\\.");
            if (parts.length == 4) {
                variants.add(parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3]);
                variants.add(parts[0] + "." + parts[1] + "." + parts[2] + ".0");
            }
        }
        return variants;
    }

    private String expandIpv6(String ip) {
        if (ip == null || !ip.contains(":")) return null;
        try {
            byte[] addr = java.net.InetAddress.getByName(ip).getAddress();
            if (addr.length == 16) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 16; i += 2) {
                    if (i > 0) sb.append(":");
                    sb.append(String.format("%02x%02x", addr[i] & 0xFF, addr[i + 1] & 0xFF));
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String compressIpv6(String ip) {
        if (ip == null || !ip.contains(":")) return null;
        try {
            return java.net.InetAddress.getByName(ip).getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }
}
