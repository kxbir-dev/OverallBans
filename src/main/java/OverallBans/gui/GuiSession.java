package OverallBans.gui;

import org.bukkit.entity.Player;

import java.util.UUID;

public class GuiSession {

    private final UUID adminUuid;
    private Player targetPlayer;
    private String targetName, targetUuid, targetIp;
    private boolean offlineTarget;
    private String punishmentTypeKey;
    private boolean isTemporary, needsTime;
    private String timeInput, whomutedInput, reasonInput;
    private boolean timeExplicit, whomutedExplicit, reasonExplicit;
    private String currentStep;
    private boolean transferring;
    private AnvilHolder currentAnvilHolder;

    private String checkTargetName;
    private String checkTargetUuid;
    private String checkTargetIp;
    private boolean checkTargetOnline;
    private boolean checkFromCommand;

    public GuiSession(UUID adminUuid) {
        this.adminUuid = adminUuid;
        this.timeExplicit = false;
        this.whomutedExplicit = false;
        this.reasonExplicit = false;
        this.transferring = false;
        this.offlineTarget = false;
        this.checkFromCommand = false;
    }

    public UUID getAdminUuid() { return adminUuid; }

    public Player getTargetPlayer() { return targetPlayer; }
    public void setTargetPlayer(Player targetPlayer) {
        this.targetPlayer = targetPlayer;
        if (targetPlayer != null) {
            this.targetName = targetPlayer.getName();
            this.targetUuid = targetPlayer.getUniqueId().toString();
            this.targetIp = targetPlayer.getAddress() == null ? null
                    : targetPlayer.getAddress().getAddress().getHostAddress();
            this.offlineTarget = false;
        }
    }

    public void setOfflineTarget(String name) {
        this.targetName = name;
        this.targetUuid = null;
        this.targetIp = null;
        this.targetPlayer = null;
        this.offlineTarget = true;
    }

    public boolean isOfflineTarget() { return offlineTarget; }

    public String getTargetName() { return targetName; }

    public String getPunishmentTypeKey() { return punishmentTypeKey; }
    public void setPunishmentTypeKey(String key) { this.punishmentTypeKey = key; }

    public boolean isTemporary() { return isTemporary; }
    public void setTemporary(boolean v) { isTemporary = v; }
    public boolean isNeedsTime() { return needsTime; }
    public void setNeedsTime(boolean v) { needsTime = v; }

    public String getTimeInput() { return timeInput; }
    public void setTimeInput(String v) { this.timeInput = v; this.timeExplicit = v != null && !v.trim().isEmpty(); }
    public boolean isTimeExplicit() { return timeExplicit; }

    public String getWhomutedInput() { return whomutedInput; }
    public void setWhomutedInput(String v) { this.whomutedInput = v; this.whomutedExplicit = v != null && !v.trim().isEmpty(); }
    public boolean isWhomutedExplicit() { return whomutedExplicit; }

    public String getReasonInput() { return reasonInput; }
    public void setReasonInput(String v) { this.reasonInput = v; this.reasonExplicit = v != null && !v.trim().isEmpty(); }
    public boolean isReasonExplicit() { return reasonExplicit; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String step) { this.currentStep = step; }

    public boolean isTransferring() { return transferring; }
    public void setTransferring(boolean v) { transferring = v; }

    public AnvilHolder getCurrentAnvilHolder() { return currentAnvilHolder; }
    public void setCurrentAnvilHolder(AnvilHolder holder) { this.currentAnvilHolder = holder; }

    public String getCheckTargetName() { return checkTargetName; }
    public String getCheckTargetUuid() { return checkTargetUuid; }
    public String getCheckTargetIp() { return checkTargetIp; }
    public boolean isCheckTargetOnline() { return checkTargetOnline; }

    public void setCheckTarget(String name, String uuid, String ip, boolean online) {
        this.checkTargetName = name;
        this.checkTargetUuid = uuid;
        this.checkTargetIp = ip;
        this.checkTargetOnline = online;
    }

    public boolean isCheckFromCommand() { return checkFromCommand; }
    public void setCheckFromCommand(boolean v) { this.checkFromCommand = v; }

    public String getVariant() {
        if (whomutedExplicit && reasonExplicit) return "both";
        if (whomutedExplicit) return "whopunished-only";
        if (reasonExplicit) return "reason-only";
        return "neither";
    }
}
