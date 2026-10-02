package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.utils.MessageUtils;
import OverallBans.utils.UsernameValidator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class BasePunishmentCommand implements CommandExecutor {

    protected final PunishmentPlugin plugin;

    protected BasePunishmentCommand(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.isDisabled()) {
            sender.sendMessage(MessageUtils.colorize(plugin.getUpdateManager().getDeprecationMessage()));
            return true;
        }
        try {
            return execute(sender, command, label, args);
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing command /" + label + ": " + e.getMessage());
            e.printStackTrace();
            sender.sendMessage(MessageUtils.colorize("&cAn internal error occurred while executing this command. Check console for details."));
            return true;
        }
    }

    protected abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    protected PlayerInfo resolveTarget(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        if (!UsernameValidator.isValid(name)) return null;

        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            String ip = online.getAddress() == null ? null
                    : online.getAddress().getAddress().getHostAddress();
            return new PlayerInfo(online.getName(), online.getUniqueId(), ip, online);
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        UUID uuid = null;
        try {
            uuid = offline.getUniqueId();
        } catch (Exception ignored) {
        }
        return new PlayerInfo(name, uuid, null, null);
    }

    protected boolean isTargetValid(String name) {
        return UsernameValidator.isValid(name);
    }

    protected void sendUsage(CommandSender sender, String usage) {
        String msg = plugin.getConfigManager().getString("errors.usage-error", "&cUsage: {usage}");
        msg = msg.replace("{usage}", usage);
        sender.sendMessage(MessageUtils.colorize(msg));
    }

    protected void sendError(CommandSender sender, String key, String... placeholders) {
        String msg = plugin.getConfigManager().getString("errors." + key, "&cError: " + key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        sender.sendMessage(MessageUtils.colorize(msg));
    }

    protected boolean hasPermission(CommandSender sender, String perm) {
        if (!sender.hasPermission(perm)) {
            sendError(sender, "no-permission");
            return false;
        }
        return true;
    }

    protected String buildLogString(String label, String[] args) {
        StringBuilder sb = new StringBuilder("/").append(label);
        for (String a : args) {
            sb.append(" ").append(a);
        }
        return sb.toString();
    }

    protected static class PlayerInfo {
        public final String name;
        public final UUID uuid;
        public final String ip;
        public final Player onlinePlayer;

        PlayerInfo(String name, UUID uuid, String ip, Player onlinePlayer) {
            this.name = name;
            this.uuid = uuid;
            this.ip = ip;
            this.onlinePlayer = onlinePlayer;
        }
    }
}
