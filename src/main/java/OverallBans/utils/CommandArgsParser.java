package OverallBans.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandArgsParser {

    public static final String BY_PREFIX = "by:";

    private CommandArgsParser() {
    }

    public static ParsedArgs parse(String[] args, int startIndex) {
        String whomuted = null;
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
            if (arg.length() > BY_PREFIX.length()
                    && arg.substring(0, BY_PREFIX.length()).equalsIgnoreCase(BY_PREFIX)) {
                if (whomuted == null) {
                    whomuted = arg.substring(BY_PREFIX.length());
                }
            } else {
                if (reasonBuilder.length() > 0) {
                    reasonBuilder.append(" ");
                }
                reasonBuilder.append(arg);
            }
        }
        return new ParsedArgs(whomuted, reasonBuilder.toString());
    }

    public static String getExecutorName(CommandSender sender) {
        if (sender instanceof Player) {
            return sender.getName();
        }
        return "Console";
    }

    public static class ParsedArgs {
        private final String whomuted;
        private final String reason;

        ParsedArgs(String whomuted, String reason) {
            this.whomuted = whomuted;
            this.reason = reason;
        }

        public String getWhomuted() {
            return whomuted;
        }

        public String getReason() {
            return reason;
        }

        public boolean hasWhomuted() {
            return whomuted != null && !whomuted.isEmpty();
        }

        public boolean hasReason() {
            return reason != null && !reason.trim().isEmpty();
        }

        public String getVariant() {
            if (hasWhomuted() && hasReason()) {
                return "both";
            }
            if (hasWhomuted()) {
                return "whopunished-only";
            }
            if (hasReason()) {
                return "reason-only";
            }
            return "neither";
        }
    }
}
