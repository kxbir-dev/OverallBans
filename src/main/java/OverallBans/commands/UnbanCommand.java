package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.managers.BanManager;
import OverallBans.models.PunishmentRecord;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.UsernameValidator;
import OverallBans.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnbanCommand extends BasePunishmentCommand {

    private final boolean silent;

    public UnbanCommand(PunishmentPlugin plugin, boolean silent) {
        super(plugin);
        this.silent = silent;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        String perm = silent ? "punishment.sunban" : "punishment.unban";
        if (!hasPermission(sender, perm)) return true;

        if (args.length < 1) {
            sendUsage(sender, "/" + label + " <player> [by:unbanned_by] [reason]");
            return true;
        }

        String targetName = args[0];
        CommandArgsParser.ParsedArgs parsed = CommandArgsParser.parse(args, 1);
        String unbannedBy = parsed.hasWhomuted() ? parsed.getWhomuted() : CommandArgsParser.getExecutorName(sender);
        String reason = parsed.getReason();
        boolean hasReason = parsed.hasReason();
        boolean hasWhomuted = parsed.hasWhomuted();
        String variant = getVariant(hasWhomuted, hasReason);

        BanManager bm = plugin.getBanManager();
        if (!UsernameValidator.isValid(targetName)) {
            sendError(sender, "invalid-username", "player", targetName);
            return true;
        }

        PlayerInfo target = resolveTarget(targetName);
        String ipForLookup = target != null ? target.ip : null;

        PunishmentRecord removed = bm.removeBan(targetName);
        if (removed == null && ipForLookup != null) {
            removed = bm.removeIPBan(ipForLookup);
        }

        if (removed == null) {
            sendError(sender, "not-banned", "player", targetName);
            return true;
        }

        Map<String, String> ph = new HashMap<>();
        ph.put("player", targetName);
        ph.put("unbanned_by", unbannedBy);
        ph.put("reason", reason);

        if (silent) {
            String adminMsg = getVariantString("silent-admin-messages.sunban", variant);
            adminMsg = MessageUtils.replace(adminMsg, ph);
            if (!adminMsg.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(adminMsg));
            }
        } else {
            String confirmMsg = getVariantString("unban-confirmation", variant);
            confirmMsg = MessageUtils.replace(confirmMsg, ph);
            if (!confirmMsg.isEmpty()) {
                sender.sendMessage(MessageUtils.colorize(confirmMsg));
            }

            String broadcastMsg = getVariantString("broadcasts.unban", variant);
            broadcastMsg = MessageUtils.replace(broadcastMsg, ph);
            plugin.broadcastIfEnabled("broadcast-unbans", broadcastMsg);
        }

        String executorName = CommandArgsParser.getExecutorName(sender);
        String fullCommand = buildLogString(label, args);
        plugin.getLogManager().logPunishment(executorName, fullCommand);
        plugin.getLogManager().log("UNBAN",
                "target=" + targetName
                        + " by=" + unbannedBy
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

    private String getVariantString(String basePath, String variant) {
        String val = plugin.getConfigManager().getString(basePath + "." + variant, "");
        if (val == null || val.isEmpty()) {
            val = plugin.getConfigManager().getString(basePath + ".both", "");
        }
        return val == null ? "" : val;
    }
}
