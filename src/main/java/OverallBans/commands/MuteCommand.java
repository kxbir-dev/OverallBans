package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.models.PunishmentType;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.UsernameValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MuteCommand extends BasePunishmentCommand {

    public MuteCommand(PunishmentPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.mute")) return true;

        if (args.length < 1) {
            sendUsage(sender, "/mute <player> [by:whomuted] [reason]");
            return true;
        }

        String targetName = args[0];
        CommandArgsParser.ParsedArgs parsed = CommandArgsParser.parse(args, 1);
        String whomuted = parsed.hasWhomuted() ? parsed.getWhomuted() : CommandArgsParser.getExecutorName(sender);
        String reason = parsed.getReason();

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
                PunishmentType.MUTE,
                target,
                whomuted,
                parsed.hasWhomuted(),
                reason,
                -1L,
                null,
                sender,
                label,
                args
        );
        return true;
    }
}
