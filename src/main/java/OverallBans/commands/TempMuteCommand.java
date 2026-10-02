package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.models.PunishmentType;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.UsernameValidator;
import OverallBans.utils.TimeParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TempMuteCommand extends BasePunishmentCommand {

    public TempMuteCommand(PunishmentPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.tmute")) return true;

        if (args.length < 2) {
            sendUsage(sender, "/tmute <player> <time> [by:whomuted] [reason]");
            return true;
        }

        String targetName = args[0];
        String timeStr = args[1];
        CommandArgsParser.ParsedArgs parsed = CommandArgsParser.parse(args, 2);
        String whomuted = parsed.hasWhomuted() ? parsed.getWhomuted() : CommandArgsParser.getExecutorName(sender);
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

        if (plugin.getMuteManager().isMuted(target.name, target.uuid, target.ip)) {
            sendError(sender, "already-muted", "player", target.name);
            return true;
        }

        new PunishmentExecutor(plugin).executeMute(
                PunishmentType.TMUTE,
                target,
                whomuted,
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
