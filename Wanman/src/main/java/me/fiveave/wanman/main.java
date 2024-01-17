package me.fiveave.wanman;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class main extends JavaPlugin {
    static HashMap<Object, Integer> totaldist = new HashMap<>();
    static HashMap<Object, Boolean> incart = new HashMap<>();
    static HashMap<Object, MinecartGroup> cart = new HashMap<>();
    static HashMap<Object, Boolean> measuring = new HashMap<>();
    static HashMap<Object, Double> lastx = new HashMap<>();
    static HashMap<Object, Double> lastz = new HashMap<>();
    static HashMap<Object, Double> measuretotaldist = new HashMap<>();
    static HashMap<Object, Integer> measuretotaltime = new HashMap<>();
    static main plugin;
    public static abstractfile trainfares;
    sign var = new sign();

    @Override
    public void onEnable() {
        // Plugin startup logic
        SignAction.register(var);
        plugin = this;
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new cartevents(), this);
        trainfares = new abstractfile(this, "trainfares.yml");
        trainfares.saveDefaultConfig();
        Objects.requireNonNull(this.getCommand("wanmandist")).setExecutor(new distmeasurer());
        Objects.requireNonNull(this.getCommand("wanmanfaretable")).setExecutor(new getfaretable());
        Objects.requireNonNull(this.getCommand("wanmandist")).setTabCompleter(new distmeasurer());
        Objects.requireNonNull(this.getCommand("wanmanfaretable")).setTabCompleter(new getfaretable());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        SignAction.unregister(var);
    }

    // fta = Fare Table Add Item
    // ft = Fare Table
    public static ArrayList<Integer> ft = new ArrayList<>();

    public static void fta(int i1, int i2, int inFare) {
        for (int i = i1; i <= i2; i++) {
            ft.add(i, inFare);
        }
    }

    public static String wmhead = ChatColor.YELLOW + "[" + ChatColor.GOLD + "Wanman" + ChatColor.YELLOW + "] ";
}
