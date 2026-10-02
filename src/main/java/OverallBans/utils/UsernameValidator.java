package OverallBans.utils;

import java.util.regex.Pattern;

public final class UsernameValidator {

    private static final Pattern MINECRAFT_USERNAME =
            Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    private UsernameValidator() {}

    public static boolean isValid(String username) {
        if (username == null) return false;
        return MINECRAFT_USERNAME.matcher(username).matches();
    }

    public static String getRules() {
        return "Minecraft usernames must be 3-16 characters long and contain only letters, numbers, and underscores.";
    }
}
