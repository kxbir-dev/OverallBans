package OverallBans.models;

public enum PunishmentType {
    BAN("ban", false, false, false),
    TBAN("tban", true, false, false),
    IPBAN("ipban", false, true, false),
    IPTBAN("iptban", true, true, false),
    SBAN("sban", false, false, true),
    STBAN("stban", true, false, true),
    IPSBAN("ipsban", false, true, true),
    IPSTBAN("ipstban", true, true, true),

    MUTE("mute", false, false, false),
    TMUTE("tmute", true, false, false),
    IPMUTE("ipmute", false, true, false),
    IPTMUTE("iptmute", true, true, false),
    SMUTE("smute", false, false, true),
    STMUTE("stmute", true, false, true),

    KICK("kick", false, false, false),

    UNBAN("unban", false, false, false),
    SUNBAN("sunban", false, false, true),
    UNMUTE("unmute", false, false, false),
    SUNMUTE("sunmute", false, false, true);

    private final String key;
    private final boolean temporary;
    private final boolean ipBased;
    private final boolean silent;

    PunishmentType(String key, boolean temporary, boolean ipBased, boolean silent) {
        this.key = key;
        this.temporary = temporary;
        this.ipBased = ipBased;
        this.silent = silent;
    }

    public String getKey() {
        return key;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public boolean isIpBased() {
        return ipBased;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isBan() {
        return this == BAN || this == TBAN || this == IPBAN || this == IPTBAN
                || this == SBAN || this == STBAN || this == IPSBAN || this == IPSTBAN;
    }

    public boolean isMute() {
        return this == MUTE || this == TMUTE || this == IPMUTE || this == IPTMUTE
                || this == SMUTE || this == STMUTE;
    }
}
