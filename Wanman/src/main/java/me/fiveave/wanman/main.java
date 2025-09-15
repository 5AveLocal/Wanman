package me.fiveave.wanman;

import com.bergerkiller.bukkit.tc.signactions.SignAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class main extends JavaPlugin {
    // fta = Fare Table Add Item
    // ft = Fare Table
    public final static ArrayList<Integer> ft = new ArrayList<>();
    public final static String wmhead = ChatColor.YELLOW + "[" + ChatColor.GOLD + "Wanman" + ChatColor.YELLOW + "] ";
    final static HashMap<Player, wanmanuser> wmuser = new HashMap<>();
    public static abstractfile trainfares;
    static main plugin;
    final adddistsign var = new adddistsign();

    public static void fta(int i1, int i2, int inFare) {
        for (int i = i1; i <= i2; i++) {
            ft.add(i, inFare);
        }
    }

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
}
