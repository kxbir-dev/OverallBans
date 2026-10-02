package OverallBans.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MessageUtils {

    private MessageUtils() {
    }

    public static String colorize(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> colorize(List<String> input) {
        List<String> out = new ArrayList<>();
        if (input == null) return out;
        for (String s : input) {
            out.add(colorize(s));
        }
        return out;
    }

    public static String replace(String input, String... pairs) {
        if (input == null) return "";
        Map<String, String> map = new LinkedHashMap<>();
        if (pairs != null) {
            for (int i = 0; i + 1 < pairs.length; i += 2) {
                if (pairs[i] == null) continue;
                String val = pairs[i + 1] == null ? "" : pairs[i + 1];
                map.put(pairs[i], val);
            }
        }
        String result = input;
        for (Map.Entry<String, String> e : map.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }
        return result;
    }

    public static String replace(String input, Map<String, String> placeholders) {
        if (input == null) return "";
        String result = input;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                if (e.getKey() == null) continue;
                String val = e.getValue() == null ? "" : e.getValue();
                result = result.replace("{" + e.getKey() + "}", val);
            }
        }
        return result;
    }

    public static List<String> replaceAll(List<String> input, Map<String, String> placeholders) {
        List<String> out = new ArrayList<>();
        if (input == null) return out;
        for (String s : input) {
            out.add(replace(s, placeholders));
        }
        return out;
    }

    public static String format(String input, Map<String, String> placeholders) {
        return colorize(replace(input, placeholders));
    }

    public static List<String> formatAll(List<String> input, Map<String, String> placeholders) {
        List<String> out = new ArrayList<>();
        if (input == null) return out;
        for (String s : input) {
            out.add(format(s, placeholders));
        }
        return out;
    }

    public static String joinLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(lines.get(i));
        }
        return sb.toString();
    }
}
