package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.models.PunishmentType;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.UsernameValidator;
import OverallBans.utils.TimeParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SIPTempBanCommand extends BasePunishmentCommand {

    public SIPTempBanCommand(PunishmentPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.ipstban")) return true;

        if (args.length < 2) {
            sendUsage(sender, "/ipstban <player> <time> [by:whomuted] [reason]");
            return true;
        }

        String targetName = args[0];
        String timeStr = args[1];
        CommandArgsParser.ParsedArgs parsed = CommandArgsParser.parse(args, 2);
        String bannedBy = parsed.hasWhomuted() ? parsed.getWhomuted() : CommandArgsParser.getExecutorName(sender);
        String reason = parsed.getReason();

        long duration = TimeParser.parseToMillis(timeStr);
        if (duration <= 0) {
            sendError(sender, "invalid-time");
            return true;
        }

        if (!UsernameValidator.isValid(targetName)) {
            sendError(sender, "invalid-username", "player", targetName);
            return true;
        }

        PlayerInfo target = resolveTarget(targetName);
        if (target == null) {
            sendError(sender, "player-not-found", "player", targetName);
            return true;
        }

        if (target.ip == null || target.ip.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getString(
                    "errors.player-not-online",
                    "&cPlayer &f{player} &cmust be online to IP-ban.")
                    .replace("{player}", target.name));
            return true;
        }

        if (plugin.getBanManager().isBanned(target.name, target.uuid, target.ip)) {
            sendError(sender, "already-banned", "player", target.name);
            return true;
        }

        new PunishmentExecutor(plugin).executeBan(
                PunishmentType.IPSTBAN,
                target,
                bannedBy,
                parsed.hasWhomuted(),
                reason,
                duration,
                timeStr,
                sender,
                label,
                args
        );
        return true;
    }
}
