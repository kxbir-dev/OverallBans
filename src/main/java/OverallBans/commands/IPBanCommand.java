package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.models.PunishmentType;
import OverallBans.utils.CommandArgsParser;
import OverallBans.utils.UsernameValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class IPBanCommand extends BasePunishmentCommand {

    public IPBanCommand(PunishmentPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.ipban")) return true;

        if (args.length < 1) {
            sendUsage(sender, "/ipban <player> [by:whomuted] [reason]");
            return true;
        }

        String targetName = args[0];
        CommandArgsParser.ParsedArgs parsed = CommandArgsParser.parse(args, 1);
        String bannedBy = parsed.hasWhomuted() ? parsed.getWhomuted() : CommandArgsParser.getExecutorName(sender);
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
                PunishmentType.IPBAN,
                target,
                bannedBy,
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
