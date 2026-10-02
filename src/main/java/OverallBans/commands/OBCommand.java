package OverallBans.commands;

import OverallBans.PunishmentPlugin;
import OverallBans.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OBCommand extends BasePunishmentCommand implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("reload");

    public OBCommand(PunishmentPlugin plugin) { super(plugin); }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "punishment.editor")) return true;

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            if (plugin.getUpdateManager() != null) plugin.getUpdateManager().setupChecker();
            sender.sendMessage(MessageUtils.colorize("&aConfiguration reloaded."));
            return true;
        }

        showHelp(sender);
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(MessageUtils.colorize("&8&l&m============&r &c&lOverallBans &8&l&m============"));
        sender.sendMessage(MessageUtils.colorize(""));
        sender.sendMessage(MessageUtils.colorize("&c&lBans:"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/ban <player> [by:whopunished] [reason] &7- Permanent ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/tban <player> <time> [by:whopunished] [reason] &7- Temp ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/ipban <player> [by:whopunished] [reason] &7- IP ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/iptban <player> <time> [by:whopunished] [reason] &7- IP temp ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/sban <player> [by:whopunished] [reason] &7- Silent ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/stban <player> <time> [by:whopunished] [reason] &7- Silent temp ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/ipsban <player> [by:whopunished] [reason] &7- Silent IP ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/ipstban <player> <time> [by:whopunished] [reason] &7- Silent IP temp ban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/unban <player> [by:whopunished] [reason] &7- Unban"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/sunban <player> [by:whopunished] &7- Silent unban"));
        sender.sendMessage(MessageUtils.colorize(""));
        sender.sendMessage(MessageUtils.colorize("&c&lMutes:"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/mute <player> [by:whopunished] [reason] &7- Permanent mute"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/tmute <player> <time> [by:whopunished] [reason] &7- Temp mute"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/ipmute <player> [by:whopunished] [reason] &7- IP mute"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/iptmute <player> <time> [by:whopunished] [reason] &7- IP temp mute"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/smute <player> [by:whopunished] [reason] &7- Silent mute"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/stmute <player> <time> [by:whopunished] [reason] &7- Silent temp mute"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/unmute <player> [by:whopunished] [reason] &7- Unmute"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/sunmute <player> [by:whopunished] &7- Silent unmute"));
        sender.sendMessage(MessageUtils.colorize(""));
        sender.sendMessage(MessageUtils.colorize("&c&lOther:"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/kick <player> [by:whopunished] [reason] &7- Kick"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/check <player> &7- Check active punishments"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/obg &7- Open GUI editor"));
        sender.sendMessage(MessageUtils.colorize("&8» &f/ob reload &7- Reload config"));
        sender.sendMessage(MessageUtils.colorize(""));
        sender.sendMessage(MessageUtils.colorize("&7Time format: &f1y6h30m&7, &f90d&7, &f2h&7, &f30m"));
        sender.sendMessage(MessageUtils.colorize("&7Placeholders: &f{player}&7, &f{whopunished}&7, &f{reason}&7, &f{time}&7, &f{time_left}"));
        sender.sendMessage(MessageUtils.colorize("&8&l&m========================================"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("punishment.editor")) return new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String sub : SUBCOMMANDS) if (sub.startsWith(prefix)) matches.add(sub);
            return matches;
        }
        return new ArrayList<>();
    }
}
