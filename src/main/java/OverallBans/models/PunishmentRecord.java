package OverallBans.models;

public class PunishmentRecord {
    private PunishmentType type;
    private String targetName;
    private String targetUUID;
    private String targetIP;
    private String issuedBy;
    private String reason;
    private long issuedAt;
    private long expiresAt;
    private String originalTime;
    private boolean silent;
    private boolean whomutedExplicit;

    public PunishmentRecord() {
    }

    public PunishmentRecord(PunishmentType type, String targetName, String targetUUID, String targetIP,
                            String issuedBy, String reason, long issuedAt, long expiresAt,
                            String originalTime, boolean silent, boolean whomutedExplicit) {
        this.type = type;
        this.targetName = targetName;
        this.targetUUID = targetUUID;
        this.targetIP = targetIP;
        this.issuedBy = issuedBy;
        this.reason = reason == null ? "" : reason;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.originalTime = originalTime;
        this.silent = silent;
        this.whomutedExplicit = whomutedExplicit;
    }

    public PunishmentType getType() {
        return type;
    }

    public void setType(PunishmentType type) {
        this.type = type;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(String targetUUID) {
        this.targetUUID = targetUUID;
    }

    public String getTargetIP() {
        return targetIP;
    }

    public void setTargetIP(String targetIP) {
        this.targetIP = targetIP;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getReason() {
        return reason == null ? "" : reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(String originalTime) {
        this.originalTime = originalTime;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isWhomutedExplicit() {
        return whomutedExplicit;
    }

    public void setWhomutedExplicit(boolean whomutedExplicit) {
        this.whomutedExplicit = whomutedExplicit;
    }

    public boolean isPermanent() {
        return expiresAt == -1L;
    }

    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() >= expiresAt;
    }

    public String getVariant() {
        boolean hasWhomuted = whomutedExplicit;
        boolean hasReason = reason != null && !reason.trim().isEmpty();
        if (hasWhomuted && hasReason) return "both";
        if (hasWhomuted) return "whopunished-only";
        if (hasReason) return "reason-only";
        return "neither";
    }
}
