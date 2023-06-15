package me.fiveave.wanman;

import me.fiveave.wanman.main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static me.fiveave.wanman.main.wmhead;

public class abstractfile {
    protected final main plugin;
    private File file;
    public FileConfiguration dataconfig;
    public FileConfiguration oldconfig;

    public abstractfile(main plugin, String fileName) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), fileName);
        dataconfig = YamlConfiguration.loadConfiguration(file);
        oldconfig = YamlConfiguration.loadConfiguration(file);
        saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        InputStream stream = plugin.getResource(file.getName());
        if (stream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            if (file.exists() && !dataconfig.getKeys(true).containsAll(defaultConfig.getKeys(true))) {
                dataconfig.setDefaults(defaultConfig);
                plugin.saveResource(file.getName(), true);
                dataconfig = YamlConfiguration.loadConfiguration(file);
                for (String str : dataconfig.getKeys(true)) {
                    if (!Objects.equals(oldconfig.get(str), dataconfig.get(str))) {
                        if (oldconfig.get(str) != null) {
                            dataconfig.set(str, oldconfig.get(str));
                        }
                    }
                    if (oldconfig.get(str) instanceof String) {
                        dataconfig.set(str, dataconfig.get(str));
                    }
                }
                plugin.saveResource(file.getName(), true);
                try {
                    dataconfig.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(wmhead + ChatColor.YELLOW + file.getName() + " has been updated due to missing content");
            }
        }
    }

    public void save() {
        if (dataconfig == null || file == null) {
            return;
        }
        try {
            dataconfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (file == null) {
            assert false;
            file = new File(plugin.getDataFolder(), file.getName());
        }
        if (!file.exists()) {
            plugin.saveResource(file.getName(), false);
            dataconfig = YamlConfiguration.loadConfiguration(file);
        }
    }
}