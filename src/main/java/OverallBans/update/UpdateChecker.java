package OverallBans.update;

public interface UpdateChecker {

    UpdateInfo checkForUpdate() throws Exception;

    String getSourceName();
}
