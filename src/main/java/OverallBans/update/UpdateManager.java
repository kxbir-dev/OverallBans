package OverallBans.update;

import OverallBans.PunishmentPlugin;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicReference;

public class UpdateManager {

    private static final String GITHUB_API_URL =
            "https://api.github.com/repos/kxbir-dev/OverallBans/releases/latest";
    private static final String MODRINTH_URL = "https://modrinth.com/mod/overallbans";
    private static final String DEPRECATION_MSG =
            "&7[&cOverallBans&7] &cThis version of the plugin is deprecated and no longer supported. Download the latest version at: &e" + MODRINTH_URL;

    private final PunishmentPlugin plugin;
    private final AtomicReference<UpdateInfo> lastResult = new AtomicReference<>();
    private final AtomicReference<Boolean> disabled = new AtomicReference<>(false);
    private final AtomicReference<Boolean> updateAvailable = new AtomicReference<>(false);
    private final AtomicReference<String> latestVersion = new AtomicReference<>("");
    private GitHubUpdateChecker checker;

    public UpdateManager(PunishmentPlugin plugin) {
        this.plugin = plugin;
        setupChecker();
    }

    public void setupChecker() {
        this.checker = null;
        String currentVersion = plugin.getDescription().getVersion();
        this.checker = new GitHubUpdateChecker(GITHUB_API_URL, currentVersion);
    }

    public void checkOnStartup() {
        if (checker == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UpdateInfo info = null;
            try {
                info = checker.checkForUpdate();
                lastResult.set(info);
            } catch (Exception e) {
                return;
            }
            if (info == null) return;
            int status = VersionComparator.compare(info.getCurrentVersion(), info.getLatestVersion());
            if (status == VersionComparator.DISABLE) {
                disabled.set(true);
                plugin.setDisabled(true);
                plugin.getLogger().severe("[OverallBans] This version is deprecated. Update required.");
            } else if (status == VersionComparator.ALERT) {
                updateAvailable.set(true);
                latestVersion.set(info.getLatestVersion());
            }
        });
    }

    public String getDeprecationMessage() {
        return DEPRECATION_MSG;
    }

    public String getUpdateMessage() {
        String latest = latestVersion.get();
        String current = plugin.getDescription().getVersion();
        return "&7[&cOverallBans&7] &7A new version is available: &a" + latest
                + " &7(current: &c" + current + "&7)\n&7Download: &e" + MODRINTH_URL;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public boolean isUpdateAvailable() {
        return updateAvailable.get();
    }

    public UpdateInfo getLastResult() {
        return lastResult.get();
    }
}
