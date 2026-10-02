package OverallBans.managers;

import OverallBans.PunishmentPlugin;
import OverallBans.models.PunishmentRecord;
import OverallBans.models.PunishmentType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BanManager {

    private final PunishmentPlugin plugin;
    private final File bansFile;
    private final File ipBansFile;
    private FileConfiguration bansConfig;
    private FileConfiguration ipBansConfig;

    private final Map<String, PunishmentRecord> playerBans = new ConcurrentHashMap<>();
    private final Map<String, PunishmentRecord> ipBans = new ConcurrentHashMap<>();

    public BanManager(PunishmentPlugin plugin) {
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        File dataDir = new File(dataFolder, "data");
        if (!dataDir.exists()) dataDir.mkdirs();
        this.bansFile = new File(dataDir, "bans.yml");
        this.ipBansFile = new File(dataDir, "ipbans.yml");
    }

    public void load() {
        bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        ipBansConfig = YamlConfiguration.loadConfiguration(ipBansFile);

        playerBans.clear();
        ConfigurationSection sec = bansConfig.getConfigurationSection("bans");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection sub = sec.getConfigurationSection(key);
                if (sub != null) {
                    PunishmentRecord rec = deserialize(sub);
                    if (rec != null) {
                        if (rec.isExpired()) {
                            continue;
                        }
                        playerBans.put(key.toLowerCase(), rec);
                    }
                }
            }
        }

        ipBans.clear();
        ConfigurationSection ipSec = ipBansConfig.getConfigurationSection("ipbans");
        if (ipSec != null) {
            for (String key : ipSec.getKeys(false)) {
                ConfigurationSection sub = ipSec.getConfigurationSection(key);
                if (sub != null) {
                    PunishmentRecord rec = deserialize(sub);
                    if (rec != null) {
                        if (rec.isExpired()) {
                            continue;
                        }
                        ipBans.put(key, rec);
                    }
                }
            }
        }
    }

    public void save() {
        bansConfig.set("bans", null);
        ConfigurationSection sec = bansConfig.createSection("bans");
        for (Map.Entry<String, PunishmentRecord> e : playerBans.entrySet()) {
            ConfigurationSection sub = sec.createSection(e.getKey());
            serialize(sub, e.getValue());
        }
        try {
            bansConfig.save(bansFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save bans.yml: " + ex.getMessage());
        }

        ipBansConfig.set("ipbans", null);
        ConfigurationSection ipSec = ipBansConfig.createSection("ipbans");
        for (Map.Entry<String, PunishmentRecord> e : ipBans.entrySet()) {
            ConfigurationSection sub = ipSec.createSection(e.getKey());
            serialize(sub, e.getValue());
        }
        try {
            ipBansConfig.save(ipBansFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save ipbans.yml: " + ex.getMessage());
        }
    }

    public void addBan(PunishmentRecord record) {
        if (record.getType().isIpBased()) {
            if (record.getTargetIP() != null && !record.getTargetIP().isEmpty()) {
                ipBans.put(record.getTargetIP(), record);
            }
            if (record.getTargetName() != null) {
                playerBans.put(record.getTargetName().toLowerCase(), record);
            }
        } else {
            if (record.getTargetName() != null) {
                playerBans.put(record.getTargetName().toLowerCase(), record);
            }
        }
        save();
    }

    public PunishmentRecord removeBan(String playerName) {
        String key = playerName.toLowerCase();
        PunishmentRecord removed = playerBans.remove(key);
        if (removed != null && removed.getType().isIpBased() && removed.getTargetIP() != null) {
            ipBans.remove(removed.getTargetIP());
        }
        save();
        return removed;
    }

    public PunishmentRecord removeIPBan(String ip) {
        PunishmentRecord removed = ipBans.remove(ip);
        if (removed != null && removed.getTargetName() != null) {
            PunishmentRecord playerEntry = playerBans.get(removed.getTargetName().toLowerCase());
            if (playerEntry != null && playerEntry.getType().isIpBased()
                    && ip.equals(playerEntry.getTargetIP())) {
                playerBans.remove(removed.getTargetName().toLowerCase());
            }
        }
        save();
        return removed;
    }

    public PunishmentRecord getActiveBan(String playerName, UUID uuid, String ip) {
        String key = playerName.toLowerCase();
        PunishmentRecord rec = playerBans.get(key);
        if (rec != null) {
            if (rec.isExpired()) {
                playerBans.remove(key);
                if (rec.getType().isIpBased() && rec.getTargetIP() != null) {
                    ipBans.remove(rec.getTargetIP());
                }
                save();
            } else {
                return rec;
            }
        }
        if (ip != null) {
            PunishmentRecord ipRec = ipBans.get(ip);
            if (ipRec != null) {
                if (ipRec.isExpired()) {
                    ipBans.remove(ip);
                    if (ipRec.getTargetName() != null) {
                        playerBans.remove(ipRec.getTargetName().toLowerCase());
                    }
                    save();
                } else {
                    return ipRec;
                }
            }
        }
        return null;
    }

    public boolean isBanned(String playerName, UUID uuid, String ip) {
        return getActiveBan(playerName, uuid, ip) != null;
    }

    public Map<String, PunishmentRecord> getPlayerBans() {
        return new HashMap<>(playerBans);
    }

    public Map<String, PunishmentRecord> getIpBans() {
        return new HashMap<>(ipBans);
    }

    private void serialize(ConfigurationSection s, PunishmentRecord r) {
        s.set("type", r.getType().getKey());
        s.set("target-name", r.getTargetName());
        s.set("target-uuid", r.getTargetUUID());
        s.set("target-ip", r.getTargetIP());
        s.set("issued-by", r.getIssuedBy());
        s.set("reason", r.getReason());
        s.set("issued-at", r.getIssuedAt());
        s.set("expires-at", r.getExpiresAt());
        s.set("original-time", r.getOriginalTime());
        s.set("silent", r.isSilent());
        s.set("whomuted-explicit", r.isWhomutedExplicit());
    }

    private PunishmentRecord deserialize(ConfigurationSection s) {
        try {
            String typeKey = s.getString("type");
            PunishmentType type = null;
            for (PunishmentType t : PunishmentType.values()) {
                if (t.getKey().equalsIgnoreCase(typeKey)) {
                    type = t;
                    break;
                }
            }
            if (type == null) return null;
            PunishmentRecord r = new PunishmentRecord();
            r.setType(type);
            r.setTargetName(s.getString("target-name"));
            r.setTargetUUID(s.getString("target-uuid"));
            r.setTargetIP(s.getString("target-ip"));
            r.setIssuedBy(s.getString("issued-by"));
            r.setReason(s.getString("reason", ""));
            r.setIssuedAt(s.getLong("issued-at", System.currentTimeMillis()));
            r.setExpiresAt(s.getLong("expires-at", -1L));
            r.setOriginalTime(s.getString("original-time"));
            r.setSilent(s.getBoolean("silent", false));
            r.setWhomutedExplicit(s.getBoolean("whomuted-explicit", false));
            return r;
        } catch (Exception e) {
            return null;
        }
    }
}
