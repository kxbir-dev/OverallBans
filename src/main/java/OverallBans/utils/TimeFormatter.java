package OverallBans.utils;

public final class TimeFormatter {

    private static final long SECONDS = 1000L;
    private static final long MINUTE  = 60L * SECONDS;
    private static final long HOUR    = 60L * MINUTE;
    private static final long DAY     = 24L * HOUR;
    private static final long MONTH   = 30L * DAY;
    private static final long YEAR    = 365L * DAY;

    private TimeFormatter() {
    }

    public static String formatRemaining(long remainingMillis) {
        if (remainingMillis < MINUTE) {
            return "1 minute";
        }

        long years   = remainingMillis / YEAR;
        long afterY  = remainingMillis % YEAR;
        long months  = afterY / MONTH;
        long afterM  = afterY % MONTH;
        long days    = afterM / DAY;
        long afterD  = afterM % DAY;
        long hours   = afterD / HOUR;
        long afterH  = afterD % HOUR;
        long minutes = afterH / MINUTE;

        StringBuilder sb = new StringBuilder();

        if (years > 0) {
            sb.append(years).append(years == 1 ? " year" : " years");
            if (months > 0) {
                sb.append(", ").append(months).append(months == 1 ? " month" : " months");
            }
            return sb.toString();
        }

        if (months > 0) {
            sb.append(months).append(months == 1 ? " month" : " months");
            if (days > 0) {
                sb.append(", ").append(days).append(days == 1 ? " day" : " days");
            }
            return sb.toString();
        }

        if (days > 0) {
            sb.append(days).append(days == 1 ? " day" : " days");
            if (hours > 0) {
                sb.append(", ").append(hours).append(hours == 1 ? " hour" : " hours");
            }
            return sb.toString();
        }

        if (hours > 0) {
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
            if (minutes > 0) {
                sb.append(", ").append(minutes).append(minutes == 1 ? " minute" : " minutes");
            }
            return sb.toString();
        }

        return minutes + (minutes == 1 ? " minute" : " minutes");
    }

    public static String formatPermanent() {
        return "permanent";
    }
}
