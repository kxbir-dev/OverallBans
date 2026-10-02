package OverallBans.managers;

import OverallBans.PunishmentPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private static final String CONFIG_VERSION = "1.6.2";

    private final PunishmentPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(PunishmentPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultsStream = plugin.getResource("config.yml");
        if (defaultsStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultsStream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }
    }

    public void reload() {
        load();
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            load();
        }
        return config;
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    public boolean getBoolean(String path, boolean def) {
        return getConfig().getBoolean(path, def);
    }

    public String getString(String path, String def) {
        return getConfig().getString(path, def);
    }

    public int getInt(String path, int def) {
        return getConfig().getInt(path, def);
    }

    public List<String> getStringList(String path) {
        if (getConfig().isList(path)) {
            List<String> list = getConfig().getStringList(path);
            if (list == null) list = new ArrayList<>();
            return list;
        }
        return new ArrayList<>();
    }

    public Map<String, String> getSectionKeysAsMap(String path) {
        Map<String, String> map = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection(path);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Object val = section.get(key);
                map.put(key, val == null ? "" : val.toString());
            }
        }
        return map;
    }
}
