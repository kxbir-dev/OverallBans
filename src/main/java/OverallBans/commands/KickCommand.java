package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.MessageUtils;
import OverallBans.utils.UsernameValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KickCommand extends BasePunishmentCommand {

    public KickCommand(PunishmentPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.kick")) return true;

        if (args.length < 1) {
            sendUsage(sender, "/kick <player> [by:kicked_by] [reason]");
            return true;
        }

        String targetName = args[0];
        CommandArgsParser.ParsedArgs parsed = CommandArgsParser.parse(args, 1);
        String kickedBy = parsed.hasWhomuted() ? parsed.getWhomuted() : CommandArgsParser.getExecutorName(sender);
        String reason = parsed.getReason();
        boolean hasReason = parsed.hasReason();
        boolean hasWhomuted = parsed.hasWhomuted();
        String variant = getVariant(hasWhomuted, hasReason);

        if (!UsernameValidator.isValid(targetName)) {
            sendError(sender, "invalid-username", "player", targetName);
            return true;
        }

        Player target = plugin.getServer().getPlayerExact(targetName);
        if (target == null) {
            sendError(sender, "player-not-online", "player", targetName);
            return true;
        }

        Map<String, String> ph = new HashMap<>();
        ph.put("player", target.getName());
        ph.put("kicked_by", kickedBy);
        ph.put("reason", reason);

        List<String> screenLines = getVariantList("kick-screens", variant);
        List<String> formatted = MessageUtils.formatAll(screenLines, ph);
        String kickMessage = MessageUtils.joinLines(formatted);

        target.kickPlayer(kickMessage);

        String broadcastMsg = getVariantString("broadcasts.kick", variant);
        broadcastMsg = MessageUtils.replace(broadcastMsg, ph);
        plugin.broadcastIfEnabled("broadcast-kicks", broadcastMsg);

        String adminConfirm = getVariantString("admin-confirmations.kick", variant);
        adminConfirm = MessageUtils.replace(adminConfirm, ph);
        if (!adminConfirm.isEmpty()) {
            sender.sendMessage(MessageUtils.colorize(adminConfirm));
        }

        String executorName = CommandArgsParser.getExecutorName(sender);
        String fullCommand = buildLogString(label, args);
        plugin.getLogManager().logPunishment(executorName, fullCommand);
        plugin.getLogManager().log("KICK",
                "target=" + target.getName()
                        + " by=" + kickedBy
                        + " executor=" + executorName
                        + (reason.isEmpty() ? "" : " reason=" + reason));
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
