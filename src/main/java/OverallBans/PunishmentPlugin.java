package OverallBans;

import OverallBans.commands.*;
import OverallBans.gui.GuiItems;
import OverallBans.gui.GuiManager;
import OverallBans.listeners.*;
import OverallBans.managers.*;
import OverallBans.models.PunishmentRecord;
import OverallBans.update.UpdateManager;
import OverallBans.utils.MessageUtils;
import OverallBans.utils.TimeFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PunishmentPlugin extends JavaPlugin {

    private static PunishmentPlugin instance;

    private ConfigManager configManager;
    private BanManager banManager;
    private MuteManager muteManager;
    private LogManager logManager;
    private AntiSpamManager antiSpamManager;
    private GuiManager guiManager;
    private UpdateManager updateManager;
    private long startTime;
    private volatile boolean disabled = false;

    @Override
    public void onEnable() {
        instance = this;
        startTime = System.currentTimeMillis();

        configManager = new ConfigManager(this);
        configManager.load();
        GuiItems.init(this);

        banManager = new BanManager(this);
        banManager.load();

        muteManager = new MuteManager(this);
        muteManager.load();

        logManager = new LogManager(this);
        logManager.logServerStarted();

        antiSpamManager = new AntiSpamManager(this);
        guiManager = new GuiManager();
        updateManager = new UpdateManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("OverallBans v" + getDescription().getVersion() + " enabled.");

        updateManager.checkOnStartup();
    }

    @Override
    public void onDisable() {
        if (guiManager != null) guiManager.clearAll();
        if (logManager != null) logManager.logServerStopped();
        if (banManager != null) banManager.save();
        if (muteManager != null) muteManager.save();
        getLogger().info("OverallBans disabled.");
    }

    public static PunishmentPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public BanManager getBanManager() { return banManager; }
    public MuteManager getMuteManager() { return muteManager; }
    public LogManager getLogManager() { return logManager; }
    public AntiSpamManager getAntiSpamManager() { return antiSpamManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public UpdateManager getUpdateManager() { return updateManager; }
    public long getStartTime() { return startTime; }

    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    @Override
    public FileConfiguration getConfig() { return configManager.getConfig(); }

    private void registerCommands() {
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("tban").setExecutor(new TempBanCommand(this));
        getCommand("ipban").setExecutor(new IPBanCommand(this));
        getCommand("iptban").setExecutor(new TempIPBanCommand(this));
        getCommand("sban").setExecutor(new SBanCommand(this));
        getCommand("stban").setExecutor(new STempBanCommand(this));
        getCommand("ipsban").setExecutor(new SIPBanCommand(this));
        getCommand("ipstban").setExecutor(new SIPTempBanCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this, false));
        getCommand("sunban").setExecutor(new UnbanCommand(this, true));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("tmute").setExecutor(new TempMuteCommand(this));
        getCommand("ipmute").setExecutor(new IPMuteCommand(this));
        getCommand("iptmute").setExecutor(new TempIPMuteCommand(this));
        getCommand("smute").setExecutor(new SmuteCommand(this));
        getCommand("stmute").setExecutor(new STempMuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this, false));
        getCommand("sunmute").setExecutor(new UnmuteCommand(this, true));
        getCommand("overallbans").setExecutor(new OverallBansCommand(this));
        getCommand("check").setExecutor(new CheckCommand(this));
        OBCommand obCommand = new OBCommand(this);
        getCommand("ob").setExecutor(obCommand);
        getCommand("ob").setTabCompleter(obCommand);
        getCommand("obg").setExecutor(new OBGCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new UpdateNotifyListener(this), this);
    }

    public Map<String, String> buildPlaceholders(PunishmentRecord rec) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", rec.getTargetName() == null ? "" : rec.getTargetName());
        ph.put("banned_by", rec.getIssuedBy() == null ? "" : rec.getIssuedBy());
        ph.put("whomuted", rec.getIssuedBy() == null ? "" : rec.getIssuedBy());
        ph.put("unbanned_by", rec.getIssuedBy() == null ? "" : rec.getIssuedBy());
        ph.put("unmuted_by", rec.getIssuedBy() == null ? "" : rec.getIssuedBy());
        ph.put("kicked_by", rec.getIssuedBy() == null ? "" : rec.getIssuedBy());
        ph.put("reason", rec.getReason() == null ? "" : rec.getReason());
        ph.put("time", rec.getOriginalTime() == null ? "" : rec.getOriginalTime());
        if (rec.isPermanent()) {
            ph.put("time_left", "permanent");
        } else {
            long remaining = rec.getExpiresAt() - System.currentTimeMillis();
            if (remaining < 0) remaining = 0;
            ph.put("time_left", TimeFormatter.formatRemaining(remaining));
        }
        return ph;
    }

    public List<String> getStringList(String path) { return configManager.getStringList(path); }

    public void sendMsg(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sender.sendMessage(MessageUtils.colorize(message));
    }

    public void sendLines(CommandSender sender, List<String> lines) {
        if (lines == null) return;
        for (String line : lines) sender.sendMessage(MessageUtils.colorize(line));
    }

    public void broadcastIfEnabled(String flagKey, String message) {
        if (!configManager.getBoolean(flagKey, true)) return;
        if (message == null || message.isEmpty()) return;
        getServer().broadcastMessage(MessageUtils.colorize(message));
    }
}
