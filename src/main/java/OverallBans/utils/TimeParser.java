package OverallBans.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeParser {

    private static final Pattern PATTERN = Pattern.compile("(?i)(\\d+)([ydhm])");

    private static final long MILLIS_PER_MINUTE = 60L * 1000L;
    private static final long MILLIS_PER_HOUR   = 60L * MILLIS_PER_MINUTE;
    private static final long MILLIS_PER_DAY    = 24L * MILLIS_PER_HOUR;
    private static final long MILLIS_PER_YEAR   = 365L * MILLIS_PER_DAY;

    private TimeParser() {
    }

    public static long parseToMillis(String input) {
        if (input == null || input.trim().isEmpty()) {
            return -1L;
        }
        String trimmed = input.trim();
        Matcher matcher = PATTERN.matcher(trimmed);
        long total = 0L;
        boolean found = false;
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() != lastEnd) {
                String between = trimmed.substring(lastEnd, matcher.start());
                if (!between.trim().isEmpty()) {
                    return -1L;
                }
            }
            long value = Long.parseLong(matcher.group(1));
            char unit = Character.toLowerCase(matcher.group(2).charAt(0));
            switch (unit) {
                case 'y': total += value * MILLIS_PER_YEAR; break;
                case 'd': total += value * MILLIS_PER_DAY; break;
                case 'h': total += value * MILLIS_PER_HOUR; break;
                case 'm': total += value * MILLIS_PER_MINUTE; break;
                default: return -1L;
            }
            lastEnd = matcher.end();
            found = true;
        }
        if (found && lastEnd != trimmed.length()) {
            String trailing = trimmed.substring(lastEnd);
            if (!trailing.trim().isEmpty()) {
                return -1L;
            }
        }
        return found ? total : -1L;
    }
}
