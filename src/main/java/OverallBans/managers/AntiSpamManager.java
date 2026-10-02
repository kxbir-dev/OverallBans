package OverallBans.managers;

import OverallBans.PunishmentPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamManager {

    private final PunishmentPlugin plugin;
    private final Map<UUID, Deque<Long>> messageTimes = new ConcurrentHashMap<>();

    public AntiSpamManager(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getBoolean("anti-spam.enabled", true);
    }

    public int getCountInTime() {
        return plugin.getConfigManager().getInt("anti-spam.count-in-time", 3);
    }

    public int getTimeWindowSeconds() {
        return plugin.getConfigManager().getInt("anti-spam.time", 2);
    }

    public String getPunishCommand() {
        return plugin.getConfigManager().getString("anti-spam.punish", "/mute {spammer} console spamming");
    }

    public boolean recordMessage(Player player) {
        if (!isEnabled()) return false;
        if (player == null) return false;
        if (player.isOp()) return false;
        if (player.hasPermission("punishment.antispam.bypass")) return false;

        int windowSeconds = getTimeWindowSeconds();
        int threshold = getCountInTime();
        if (windowSeconds <= 0 || threshold <= 0) return false;

        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        UUID uuid = player.getUniqueId();
        Deque<Long> times = messageTimes.computeIfAbsent(uuid, k -> new LinkedList<>());

        synchronized (times) {
            while (!times.isEmpty() && (now - times.peekFirst()) > windowMillis) {
                times.pollFirst();
            }
            times.addLast(now);

            if (times.size() > threshold) {
                triggerPunish(player);
                times.clear();
                return true;
            }
        }
        return false;
    }

    private void triggerPunish(Player player) {
        String cmd = getPunishCommand();
        if (cmd == null || cmd.trim().isEmpty()) return;
        cmd = cmd.replace("{spammer}", player.getName());

        String toDispatch = cmd;
        if (toDispatch.startsWith("/")) {
            toDispatch = toDispatch.substring(1);
        }

        final String finalCmd = toDispatch;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            plugin.getLogger().info("Anti-spam triggered for " + player.getName()
                    + ": executing console command: " + finalCmd);
        });
    }

    public void clearPlayer(UUID uuid) {
        messageTimes.remove(uuid);
    }

    public void clearAll() {
        messageTimes.clear();
    }
}
