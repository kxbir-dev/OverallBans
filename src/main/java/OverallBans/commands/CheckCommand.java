package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.gui.CheckMenu;
import OverallBans.gui.GuiManager;
import OverallBans.gui.GuiSession;
import OverallBans.gui.MainMenu;
import OverallBans.utils.MessageUtils;
import OverallBans.utils.UsernameValidator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CheckCommand extends BasePunishmentCommand {

    public CheckCommand(PunishmentPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.check")) return true;

        if (!(sender instanceof Player)) {
            String msg = plugin.getConfigManager().getString("errors.player-only",
                    "&cThis command can only be used in-game.");
            sender.sendMessage(MessageUtils.colorize(msg));
            return true;
        }

        if (args.length < 1) {
            sendUsage(sender, "/check <player>");
            return true;
        }

        String targetName = args[0];
        if (!UsernameValidator.isValid(targetName)) {
            sendError(sender, "invalid-username", "player", targetName);
            return true;
        }

        Player admin = (Player) sender;

        String checkUuid = null;
        String checkIp = null;
        boolean isOnline;

        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        if (onlineTarget != null) {
            checkUuid = onlineTarget.getUniqueId().toString();
            checkIp = onlineTarget.getAddress() == null ? null
                    : onlineTarget.getAddress().getAddress().getHostAddress();
            isOnline = true;
        } else {
            try {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
                if (offline != null && offline.getUniqueId() != null) {
                    checkUuid = offline.getUniqueId().toString();
                }
            } catch (Throwable t) {}
            isOnline = false;
        }

        GuiManager gm = plugin.getGuiManager();
        GuiSession session = gm.startSession(admin);
        session.setCheckTarget(targetName, checkUuid, checkIp, isOnline);
        session.setCheckFromCommand(true);

        new CheckMenu(gm, plugin).open(admin);
        return true;
    }
}
