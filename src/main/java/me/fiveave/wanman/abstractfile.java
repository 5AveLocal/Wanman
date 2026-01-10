package me.fiveave.wanman;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.logging.Level;

import static me.fiveave.wanman.main.errorLog;

public class abstractfile {
    protected final main plugin;
    final FileConfiguration oldconfig;
    private final String fileName;
    FileConfiguration dataconfig;
    private File file;

    abstractfile(main plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        file = new File(plugin.getDataFolder(), fileName);
        saveDefaultConfig();
        // Load the current config first
        dataconfig = YamlConfiguration.loadConfiguration(file);
        // Then load a copy for comparison
        oldconfig = YamlConfiguration.loadConfiguration(file);
        reloadConfig();
    }

    void reloadConfig() {
        InputStream stream = plugin.getResource(fileName);
        if (stream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            // Check if config requires updates
            boolean updatereq = false;
            for (String key : defaultConfig.getKeys(true)) {
                if (!dataconfig.contains(key)) {
                    updatereq = true;
                    break;
                }
            }

            if (updatereq) {
                // Backup current values
                Set<String> currentKeys = oldconfig.getKeys(true);
                // Reload default config (including all new keys)
                plugin.saveResource(fileName, true);
                dataconfig = YamlConfiguration.loadConfiguration(file);
                // Restore old values
                for (String key : currentKeys) {
                    Object oldobj = oldconfig.get(key);
                    if (oldobj != null) {
                        dataconfig.set(key, oldobj);
                    }
                }
                // Add new default values
                for (String key : defaultConfig.getKeys(true)) {
                    Object newobj = defaultConfig.get(key);
                    if (!currentKeys.contains(key) && newobj != null) {
                        dataconfig.set(key, newobj);
                    }
                }
                try {
                    dataconfig.save(file);

                    Bukkit.getLogger().log(Level.INFO, ChatColor.YELLOW + fileName + " has been updated with new config options");
                } catch (IOException e) {
                    errorLog(e);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    void save() {
        if (dataconfig == null || file == null) {
            return;
        }
        try {
            dataconfig.save(file);
        } catch (IOException e) {
            errorLog(e);
        }
    }

    void saveDefaultConfig() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), fileName);
        }
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
}