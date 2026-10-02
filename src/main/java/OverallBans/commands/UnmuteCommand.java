package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.managers.MuteManager;
import OverallBans.models.PunishmentRecord;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.UsernameValidator;
import OverallBans.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnmuteCommand extends BasePunishmentCommand {

    private final boolean silent;

    public UnmuteCommand(PunishmentPlugin plugin, boolean silent) {
        super(plugin);
        this.silent = silent;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        String perm = silent ? "punishment.sunmute" : "punishment.unmute";
        if (!hasPermission(sender, perm)) return true;

        if (args.length < 1) {
            sendUsage(sender, "/" + label + " <player> [by:unmuted_by] [reason]");
            return true;
        }

        String targetName = args[0];
        CommandArgsParser.ParsedArgs parsed = CommandArgsParser.parse(args, 1);
        String unmutedBy = parsed.hasWhomuted() ? parsed.getWhomuted() : CommandArgsParser.getExecutorName(sender);
        String reason = parsed.getReason();
        boolean hasReason = parsed.hasReason();
        boolean hasWhomuted = parsed.hasWhomuted();
        String variant = getVariant(hasWhomuted, hasReason);

        MuteManager mm = plugin.getMuteManager();
        if (!UsernameValidator.isValid(targetName)) {
            sendError(sender, "invalid-username", "player", targetName);
            return true;
        }

        PlayerInfo target = resolveTarget(targetName);
        String ipForLookup = target != null ? target.ip : null;

        PunishmentRecord removed = mm.removeMute(targetName);
        if (removed == null && ipForLookup != null) {
            removed = mm.removeIPMute(ipForLookup);
        }

        if (removed == null) {
            sendError(sender, "not-muted", "player", targetName);
            return true;
        }

        Map<String, String> ph = new HashMap<>();
        ph.put("player", targetName);
        ph.put("unmuted_by", unmutedBy);
        ph.put("reason", reason);

        if (target != null && target.onlinePlayer != null) {
            List<String> lines = getVariantList("unmute-messages", variant);
            for (String line : lines) {
                target.onlinePlayer.sendMessage(MessageUtils.format(line, ph));
            }
        }

        if (silent) {
            String adminMsg = getVariantString("silent-admin-messages.sunmute", variant);
            adminMsg = MessageUtils.replace(adminMsg, ph);
            if (!adminMsg.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(adminMsg));
            }
        } else {
            String broadcastMsg = getVariantString("broadcasts.unmute", variant);
            broadcastMsg = MessageUtils.replace(broadcastMsg, ph);
            plugin.broadcastIfEnabled("broadcast-mutes", broadcastMsg);

            String adminConfirm = getVariantString("admin-confirmations.unmute", variant);
            adminConfirm = MessageUtils.replace(adminConfirm, ph);
            if (!adminConfirm.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(adminConfirm));
            }
        }

        String executorName = CommandArgsParser.getExecutorName(sender);
        String fullCommand = buildLogString(label, args);
        plugin.getLogManager().logPunishment(executorName, fullCommand);
        plugin.getLogManager().log("UNMUTE",
                "target=" + targetName
                        + " by=" + unmutedBy
                        + " executor=" + executorName
                        + (reason.isEmpty() ? "" : " reason=" + reason)
                        + (silent ? " [SILENT]" : ""));
        return true;
    }

    private String getVariant(boolean hasWhomuted, boolean hasReason) {
        if (hasWhomuted && hasReason) return "both";
        if (hasWhomuted) return "whopunished-only";
        if (hasReason) return "reason-only";
        return "neither";
    }

    private List<String> getVariantList(String basePath, String variant) {
        List<String> list = plugin.getConfigManager().getStringList(basePath + "." + variant);
        if (list.isEmpty()) {
            list = plugin.getConfigManager().getStringList(basePath + ".both");
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
}
