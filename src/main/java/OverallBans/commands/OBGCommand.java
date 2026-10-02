package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.gui.MainMenu;
import OverallBans.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OBGCommand extends BasePunishmentCommand {

    public OBGCommand(PunishmentPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.editor")) return true;

        if (!(sender instanceof Player)) {
            String msg = plugin.getConfigManager().getString("errors.player-only",
                    "&cThis command can only be used in-game.");
            sender.sendMessage(MessageUtils.colorize(msg));
            return true;
        }

        Player player = (Player) sender;
        new MainMenu(plugin.getGuiManager()).open(player);
        return true;
    }
}
