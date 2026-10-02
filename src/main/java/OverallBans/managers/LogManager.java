package OverallBans.managers;

import OverallBans.PunishmentPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

public class LogManager {

    private final PunishmentPlugin plugin;
    private final File logFolder;
    private final ReentrantLock lock = new ReentrantLock();

    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogManager(PunishmentPlugin plugin) {
        this.plugin = plugin;
        this.logFolder = new File(plugin.getDataFolder(), "logs");
        if (!logFolder.exists()) logFolder.mkdirs();
    }

    private File todayFile() {
        return new File(logFolder, LocalDateTime.now().format(FILE_FMT) + ".txt");
    }

    private String nowStamp() {
        return LocalDateTime.now().format(TIME_FMT);
    }

    public void logPunishment(String executorName, String fullCommand) {
        writeLine("[" + nowStamp() + "] [PUNISHMENT] " + executorName + " used : " + fullCommand);
    }

    public void log(String tag, String message) {
        writeLine("[" + nowStamp() + "] [" + tag + "] " + message);
    }

    public void logServerStarted() { writeLine("[" + nowStamp() + "] [SERVER] Server started."); }
    public void logServerStopped() { writeLine("[" + nowStamp() + "] [SERVER] Server stopped."); }

    private void writeLine(String line) {
        lock.lock();
        try {
            File f = todayFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
                pw.println(line);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not write to log file: " + e.getMessage());
            }
        } finally {
            lock.unlock();
        }
    }
}
