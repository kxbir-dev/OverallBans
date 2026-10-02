package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.managers.BanManager;
import OverallBans.managers.MuteManager;
import OverallBans.models.PunishmentRecord;
import OverallBans.models.PunishmentType;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class PunishmentExecutor {

    private final PunishmentPlugin plugin;

    public PunishmentExecutor(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    public void executeBan(PunishmentType type,
                           BasePunishmentCommand.PlayerInfo target,
                           String issuedBy,
                           boolean hasWhomuted,
                           String reason,
                           long durationMillis,
                           String originalTime,
                           CommandSender sender,
                           String commandLabel,
                           String[] commandArgs) {

        long now = System.currentTimeMillis();
        long expiresAt = (durationMillis == -1L) ? -1L : (now + durationMillis);
        boolean hasReason = reason != null && !reason.trim().isEmpty();
        String reasonOrEmpty = hasReason ? reason : "";
        String variant = getVariant(hasWhomuted, hasReason);

        PunishmentRecord record = new PunishmentRecord(
                type,
                target.name,
                target.uuid == null ? null : target.uuid.toString(),
                type.isIpBased() ? target.ip : null,
                issuedBy,
                reasonOrEmpty,
                now,
                expiresAt,
                originalTime,
                type.isSilent(),
                hasWhomuted
        );

        BanManager bm = plugin.getBanManager();
        bm.addBan(record);

        java.util.Map<String, String> ph = plugin.buildPlaceholders(record);
        ph.put("banned_by", issuedBy);
        ph.put("reason", reasonOrEmpty);

        if (target.onlinePlayer != null) {
            List<String> screenLines = getVariantList(
                    "ban-screens." + type.getKey(),
                    "ban-screens.ban",
                    variant
            );
            List<String> formatted = MessageUtils.formatAll(screenLines, ph);
            String kickMessage = MessageUtils.joinLines(formatted);
            target.onlinePlayer.kickPlayer(kickMessage);
        }

        if (type.isSilent()) {
            String adminMsg = getVariantString(
                    "silent-admin-messages." + type.getKey(),
                    variant
            );
            adminMsg = MessageUtils.replace(adminMsg, ph);
            if (!adminMsg.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(adminMsg));
            }
        } else {
            String broadcastMsg = getVariantString(
                    "broadcasts." + type.getKey(),
                    variant
            );
            broadcastMsg = MessageUtils.replace(broadcastMsg, ph);
            plugin.broadcastIfEnabled("broadcast-punishments", broadcastMsg);

            String adminConfirm = getVariantString(
                    "admin-confirmations." + type.getKey(),
                    variant
            );
            adminConfirm = MessageUtils.replace(adminConfirm, ph);
            if (!adminConfirm.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(adminConfirm));
            }
        }

        String executorName = CommandArgsParser.getExecutorName(sender);
        String fullCommand = buildCommandString(commandLabel, commandArgs);
        plugin.getLogManager().logPunishment(executorName, fullCommand);
        plugin.getLogManager().log(type.getKey().toUpperCase(),
                "target=" + target.name
                        + (target.ip != null ? " ip=" + target.ip : "")
                        + " by=" + issuedBy
                        + " executor=" + executorName
                        + (reasonOrEmpty.isEmpty() ? "" : " reason=" + reasonOrEmpty)
                        + (originalTime != null ? " duration=" + originalTime : "")
                        + (type.isSilent() ? " [SILENT]" : ""));
    }

    public void executeMute(PunishmentType type,
                            BasePunishmentCommand.PlayerInfo target,
                            String issuedBy,
                            boolean hasWhomuted,
                            String reason,
                            long durationMillis,
                            String originalTime,
                            CommandSender sender,
                            String commandLabel,
                            String[] commandArgs) {

        long now = System.currentTimeMillis();
        long expiresAt = (durationMillis == -1L) ? -1L : (now + durationMillis);
        boolean hasReason = reason != null && !reason.trim().isEmpty();
        String reasonOrEmpty = hasReason ? reason : "";
        String variant = getVariant(hasWhomuted, hasReason);

        PunishmentRecord record = new PunishmentRecord(
                type,
                target.name,
                target.uuid == null ? null : target.uuid.toString(),
                type.isIpBased() ? target.ip : null,
                issuedBy,
                reasonOrEmpty,
                now,
                expiresAt,
                originalTime,
                type.isSilent(),
                hasWhomuted
        );

        MuteManager mm = plugin.getMuteManager();
        mm.addMute(record);

        java.util.Map<String, String> ph = plugin.buildPlaceholders(record);
        ph.put("whomuted", issuedBy);
        ph.put("reason", reasonOrEmpty);

        boolean showReceiptToPlayer = !type.isSilent()
                || plugin.getConfigManager().getBoolean(
                        "settings.silent-mute-shows-receipt-to-player", true);
        if (target.onlinePlayer != null && showReceiptToPlayer) {
            List<String> receiptLines = getVariantList(
                    "mute-receipt." + type.getKey(),
                    "mute-receipt.mute",
                    variant
            );
            for (String line : receiptLines) {
                target.onlinePlayer.sendMessage(MessageUtils.format(line, ph));
            }
        }

        if (type.isSilent()) {
            String adminMsg = getVariantString(
                    "silent-admin-messages." + type.getKey(),
                    variant
            );
            adminMsg = MessageUtils.replace(adminMsg, ph);
            if (!adminMsg.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(adminMsg));
            }
        } else {
            String broadcastMsg = getVariantString(
                    "broadcasts." + type.getKey(),
                    variant
            );
            broadcastMsg = MessageUtils.replace(broadcastMsg, ph);
            plugin.broadcastIfEnabled("broadcast-mutes", broadcastMsg);

            String adminConfirm = getVariantString(
                    "admin-confirmations." + type.getKey(),
                    variant
            );
            adminConfirm = MessageUtils.replace(adminConfirm, ph);
            if (!adminConfirm.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(adminConfirm));
            }
        }

        String executorName = CommandArgsParser.getExecutorName(sender);
        String fullCommand = buildCommandString(commandLabel, commandArgs);
        plugin.getLogManager().logPunishment(executorName, fullCommand);
        plugin.getLogManager().log(type.getKey().toUpperCase(),
                "target=" + target.name
                        + (target.ip != null ? " ip=" + target.ip : "")
                        + " by=" + issuedBy
                        + " executor=" + executorName
                        + (reasonOrEmpty.isEmpty() ? "" : " reason=" + reasonOrEmpty)
                        + (originalTime != null ? " duration=" + originalTime : "")
                        + (type.isSilent() ? " [SILENT]" : ""));
    }

    private String getVariant(boolean hasWhomuted, boolean hasReason) {
        if (hasWhomuted && hasReason) {
            return "both";
        }
        if (hasWhomuted) {
            return "whopunished-only";
        }
        if (hasReason) {
            return "reason-only";
        }
        return "neither";
    }

    private List<String> getVariantList(String basePath, String fallbackPath, String variant) {
        List<String> list = plugin.getConfigManager().getStringList(basePath + "." + variant);
        if (list.isEmpty() && fallbackPath != null) {
            list = plugin.getConfigManager().getStringList(fallbackPath + "." + variant);
        }
        if (list.isEmpty()) {
            list = plugin.getConfigManager().getStringList(basePath + ".both");
        }
        if (list.isEmpty() && fallbackPath != null) {
            list = plugin.getConfigManager().getStringList(fallbackPath + ".both");
        }
        return list;
    }

    private String getVariantString(String basePath, String variant) {
        String val = plugin.getConfigManager().getString(basePath + "." + variant, "");
        if (val == null || val.isEmpty()) {
            val = plugin.getConfigManager().getString(basePath + ".both", "");
        }
        return val == null ? "" : val;
    }

    private String buildCommandString(String label, String[] args) {
        StringBuilder sb = new StringBuilder("/").append(label);
        for (String a : args) {
            sb.append(" ").append(a);
        }
        return sb.toString();
    }

    public static String combineReason(String[] args, int start) {
        if (args == null || args.length <= start) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
