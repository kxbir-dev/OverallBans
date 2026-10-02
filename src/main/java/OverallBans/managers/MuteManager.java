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

public class MuteManager {

    private final PunishmentPlugin plugin;
    private final File mutesFile;
    private final File ipMutesFile;
    private FileConfiguration mutesConfig;
    private FileConfiguration ipMutesConfig;

    private final Map<String, PunishmentRecord> playerMutes = new ConcurrentHashMap<>();
    private final Map<String, PunishmentRecord> ipMutes = new ConcurrentHashMap<>();

    public MuteManager(PunishmentPlugin plugin) {
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        File dataDir = new File(dataFolder, "data");
        if (!dataDir.exists()) dataDir.mkdirs();
        this.mutesFile = new File(dataDir, "mutes.yml");
        this.ipMutesFile = new File(dataDir, "ipmutes.yml");
    }

    public void load() {
        mutesConfig = YamlConfiguration.loadConfiguration(mutesFile);
        ipMutesConfig = YamlConfiguration.loadConfiguration(ipMutesFile);

        playerMutes.clear();
        ConfigurationSection sec = mutesConfig.getConfigurationSection("mutes");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection sub = sec.getConfigurationSection(key);
                if (sub != null) {
                    PunishmentRecord rec = deserialize(sub);
                    if (rec != null) {
                        if (rec.isExpired()) continue;
                        playerMutes.put(key.toLowerCase(), rec);
                    }
                }
            }
        }

        ipMutes.clear();
        ConfigurationSection ipSec = ipMutesConfig.getConfigurationSection("ipmutes");
        if (ipSec != null) {
            for (String key : ipSec.getKeys(false)) {
                ConfigurationSection sub = ipSec.getConfigurationSection(key);
                if (sub != null) {
                    PunishmentRecord rec = deserialize(sub);
                    if (rec != null) {
                        if (rec.isExpired()) continue;
                        ipMutes.put(key, rec);
                    }
                }
            }
        }
    }

    public void save() {
        mutesConfig.set("mutes", null);
        ConfigurationSection sec = mutesConfig.createSection("mutes");
        for (Map.Entry<String, PunishmentRecord> e : playerMutes.entrySet()) {
            ConfigurationSection sub = sec.createSection(e.getKey());
            serialize(sub, e.getValue());
        }
        try {
            mutesConfig.save(mutesFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save mutes.yml: " + ex.getMessage());
        }

        ipMutesConfig.set("ipmutes", null);
        ConfigurationSection ipSec = ipMutesConfig.createSection("ipmutes");
        for (Map.Entry<String, PunishmentRecord> e : ipMutes.entrySet()) {
            ConfigurationSection sub = ipSec.createSection(e.getKey());
            serialize(sub, e.getValue());
        }
        try {
            ipMutesConfig.save(ipMutesFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save ipmutes.yml: " + ex.getMessage());
        }
    }

    public void addMute(PunishmentRecord record) {
        if (record.getType().isIpBased()) {
            if (record.getTargetIP() != null && !record.getTargetIP().isEmpty()) {
                ipMutes.put(record.getTargetIP(), record);
            }
            if (record.getTargetName() != null) {
                playerMutes.put(record.getTargetName().toLowerCase(), record);
            }
        } else {
            if (record.getTargetName() != null) {
                playerMutes.put(record.getTargetName().toLowerCase(), record);
            }
        }
        save();
    }

    public PunishmentRecord removeMute(String playerName) {
        String key = playerName.toLowerCase();
        PunishmentRecord removed = playerMutes.remove(key);
        if (removed != null && removed.getType().isIpBased() && removed.getTargetIP() != null) {
            ipMutes.remove(removed.getTargetIP());
        }
        save();
        return removed;
    }

    public PunishmentRecord removeIPMute(String ip) {
        PunishmentRecord removed = ipMutes.remove(ip);
        if (removed != null && removed.getTargetName() != null) {
            PunishmentRecord playerEntry = playerMutes.get(removed.getTargetName().toLowerCase());
            if (playerEntry != null && playerEntry.getType().isIpBased()
                    && ip.equals(playerEntry.getTargetIP())) {
                playerMutes.remove(removed.getTargetName().toLowerCase());
            }
        }
        save();
        return removed;
    }

    public PunishmentRecord getActiveMute(String playerName, UUID uuid, String ip) {
        String key = playerName.toLowerCase();
        PunishmentRecord rec = playerMutes.get(key);
        if (rec != null) {
            if (rec.isExpired()) {
                playerMutes.remove(key);
                if (rec.getType().isIpBased() && rec.getTargetIP() != null) {
                    ipMutes.remove(rec.getTargetIP());
                }
                save();
            } else {
                return rec;
            }
        }
        if (ip != null) {
            PunishmentRecord ipRec = ipMutes.get(ip);
            if (ipRec != null) {
                if (ipRec.isExpired()) {
                    ipMutes.remove(ip);
                    if (ipRec.getTargetName() != null) {
                        playerMutes.remove(ipRec.getTargetName().toLowerCase());
                    }
                    save();
                } else {
                    return ipRec;
                }
            }
        }
        return null;
    }

    public boolean isMuted(String playerName, UUID uuid, String ip) {
        return getActiveMute(playerName, uuid, ip) != null;
    }

    public Map<String, PunishmentRecord> getPlayerMutes() {
        return new HashMap<>(playerMutes);
    }

    public Map<String, PunishmentRecord> getIpMutes() {
        return new HashMap<>(ipMutes);
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
