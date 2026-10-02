package OverallBans.update;

public class UpdateInfo {

    private final String latestVersion;
    private final String currentVersion;
    private final String downloadUrl;
    private final String releaseUrl;
    private final int status;

    public UpdateInfo(String latestVersion, String currentVersion, String downloadUrl, String releaseUrl) {
        this.latestVersion = latestVersion;
        this.currentVersion = currentVersion;
        this.downloadUrl = downloadUrl;
        this.releaseUrl = releaseUrl;
        this.status = VersionComparator.compare(currentVersion, latestVersion);
    }

    public String getLatestVersion() { return latestVersion; }
    public String getCurrentVersion() { return currentVersion; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getReleaseUrl() { return releaseUrl; }
    public int getStatus() { return status; }
    public boolean isAlert() { return status == VersionComparator.ALERT; }
    public boolean isDisabled() { return status == VersionComparator.DISABLE; }
    public boolean isSilent() { return status == VersionComparator.SILENT; }
}
