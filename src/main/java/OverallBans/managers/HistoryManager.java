package OverallBans.managers;

import OverallBans.PunishmentPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryManager {

    private final PunishmentPlugin plugin;
    private final File logsFolder;

    private static final Pattern LOG_LINE = Pattern.compile(
            "^\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\] \\[([A-Z]+)\\] (.*)$");

    private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public HistoryManager(PunishmentPlugin plugin) {
        this.plugin = plugin;
        this.logsFolder = new File(plugin.getDataFolder(), "logs");
    }

    public List<String> getHistoryLast24Hours() {
        List<ParsedLine> all = new ArrayList<>();
        long cutoff = System.currentTimeMillis() - (24L * 60L * 60L * 1000L);
        long now = System.currentTimeMillis();
        File today = fileForDate(now);
        File yesterday = fileForDate(now - 24L * 60L * 60L * 1000L);
        if (today.exists()) all.addAll(parseFile(today));
        if (yesterday.exists() && !yesterday.equals(today)) all.addAll(parseFile(yesterday));
        List<String> result = new ArrayList<>();
        for (ParsedLine pl : all) {
            if (pl.timestamp < cutoff) continue;
            if ("PUNISHMENT".equals(pl.tag)) result.add(pl.originalLine);
        }
        return result;
    }

    public List<String> getHistoryForPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) return Collections.emptyList();
        String nameLower = playerName.toLowerCase();
        File[] files = logsFolder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return Collections.emptyList();
        List<File> sorted = new ArrayList<>();
        Collections.addAll(sorted, files);
        Collections.sort(sorted);
        List<String> result = new ArrayList<>();
        for (File f : sorted) {
            for (ParsedLine pl : parseFile(f)) {
                if (!isPunishmentTag(pl.tag) || pl.body == null) continue;
                if (pl.body.toLowerCase().contains(nameLower)) result.add(pl.originalLine);
            }
        }
        return result;
    }

    private boolean isPunishmentTag(String tag) {
        if (tag == null) return false;
        return tag.equals("PUNISHMENT") || tag.equals("BAN") || tag.equals("TBAN")
                || tag.equals("IPBAN") || tag.equals("IPTBAN") || tag.equals("SBAN")
                || tag.equals("STBAN") || tag.equals("IPSBAN") || tag.equals("IPSTBAN")
                || tag.equals("MUTE") || tag.equals("TMUTE") || tag.equals("IPMUTE")
                || tag.equals("IPTMUTE") || tag.equals("SMUTE") || tag.equals("STMUTE")
                || tag.equals("KICK") || tag.equals("UNBAN") || tag.equals("UNMUTE");
    }

    private File fileForDate(long epochMillis) {
        String name = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()).format(FILE_DATE) + ".txt";
        return new File(logsFolder, name);
    }

    private List<ParsedLine> parseFile(File file) {
        List<ParsedLine> out = new ArrayList<>();
        List<String> lines;
        try { lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8); }
        catch (IOException e) { return out; }
        for (String raw : lines) {
            if (raw == null) continue;
            Matcher m = LOG_LINE.matcher(raw);
            if (m.matches()) {
                ParsedLine pl = new ParsedLine();
                pl.originalLine = raw;
                pl.timeStr = m.group(1);
                pl.tag = m.group(2);
                pl.body = m.group(3);
                try {
                    pl.timestamp = LocalDateTime.parse(pl.timeStr, LOG_TIME)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                } catch (Exception e) { pl.timestamp = 0L; }
                out.add(pl);
            }
        }
        return out;
    }

    private static class ParsedLine {
        String originalLine, timeStr, tag, body;
        long timestamp;
    }
}
