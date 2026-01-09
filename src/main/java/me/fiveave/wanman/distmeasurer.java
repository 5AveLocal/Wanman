package me.fiveave.wanman;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.fiveave.wanman.faretable.addZeroPadding;
import static me.fiveave.wanman.main.*;
import static me.fiveave.wanman.wanmanuser.initWanmanuser;

public class distmeasurer implements CommandExecutor, TabCompleter {

    public static void measuredist(Player p, Boolean marker) {
        double dist;
        initWanmanuser(p);
        wanmanuser user = wmuser.get(p);
        if (p.isInsideVehicle() && user.isMeasuring()) {
            // Get TrainCarts vehicle or other
            if (p.getVehicle() instanceof Minecart) {
                MinecartGroup mg = MinecartGroupStore.get(p.getVehicle());
                TrainProperties tprop = mg.getProperties();
                dist = Math.min(tprop.getSpeedLimit(), mg.getAverageForce());
            } else {
                double locx = p.getLocation().getX();
                double locz = p.getLocation().getZ();
                dist = Math.hypot(locx - user.getLastx(), locz - user.getLastz());
                user.setLastx(locx);
                user.setLastz(locz);
            }
            // Update measured time and distance
            user.setMeasuretotaltime(user.getMeasuretotaltime() + 1);
            user.setMeasuretotaldist(user.getMeasuretotaldist() + dist);
            // Marker every 100 m
            int intdist = (int) user.getMeasuretotaldist();
            if (marker && Math.floorMod(intdist, 100) == 0) {
                Block blk = Objects.requireNonNull(p.getLocation().getWorld()).getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY() + 2, p.getLocation().getBlockZ());
                blk.setType(Material.OAK_SIGN, false);
                Sign sign = (Sign) blk.getState();
                sign.setLine(1, ChatColor.GOLD + "Wanman " + ChatColor.RED + "Marker");
                sign.setLine(2, ChatColor.BOLD + String.valueOf(intdist) + ChatColor.RESET + " m");
                sign.update();
            }
            // Rounding
            DecimalFormat df0 = new DecimalFormat("#");
            DecimalFormat df2 = new DecimalFormat("0.00");
            // Action bar
            String actionbarmsg = ChatColor.GOLD + "速度 Speed: " + addZeroPadding(3, ChatColor.YELLOW + df0.format(dist * 72)) + " km/h"
                    + ChatColor.YELLOW + " | " + ChatColor.GOLD + "距離 Dist: " + ChatColor.YELLOW + df2.format(user.getMeasuretotaldist()) + " m"
                    + ChatColor.YELLOW + " | " + ChatColor.GOLD + "時間 Time: " + ChatColor.YELLOW + tickToTimeFormatter(user.getMeasuretotaltime());
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbarmsg));
            Bukkit.getScheduler().runTaskLater(plugin, () -> measuredist(p, marker), 1);
        } else if (!p.isInsideVehicle()) {
            Bukkit.dispatchCommand(p, "wanmandist");
        }
    }

    static FileConfiguration getTF() {
        return trainfares.dataconfig;
    }

    public static String tickToTimeFormatter(int t) {
        int h = Math.floorDiv(t, 72000);
        t -= h * 72000;
        int m = Math.floorDiv(t, 1200);
        t -= m * 1200;
        int s = Math.floorDiv(t, 20);
        t -= s * 20;
        int q = Math.floorMod(t, 20) * 5;
        return String.format("%02d:%02d:%02d.%02d", h, m, s, q);
    }

    static double getFareFromTable(double dist, double multi) {
        double fare;
        double km2 = dist * 0.001;
        int km = (int) (km2 + 1) - 1 == km2 ? (int) km2 : (int) (km2 + 1);
        if (km > ft.size()) {
            fare = ft.get(ft.size() - 1) * multi;
        } else {
            fare = (ft.get(km)) * multi;
        }
        return fare;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equals("marker")) {
                if (!sender.hasPermission("wanman.marker")) {
                    sender.sendMessage(wmhead + ChatColor.RED + "権限不足！ No permission!");
                    return true;
                }
            }
        }
        Player p = (Player) sender;
        initWanmanuser(p);
        wanmanuser user = wmuser.get(p);
        try {
            if (!user.isMeasuring()) {
                if (p.isInsideVehicle()) {
                    user.setMeasuring(true);
                    user.setMeasuretotaldist(0);
                    user.setMeasuretotaltime(0);
                    user.setLastx(p.getLocation().getX());
                    user.setLastz(p.getLocation().getZ());
                    // Marker enabled?
                    if (args.length == 1) {
                        if (args[0].equals("marker")) {
                            measuredist(p, true);
                            p.sendMessage(wmhead + ChatColor.YELLOW + "標識は100メートルごとに配置されます。 Signs are placed every 100 meters.");
                        } else {
                            user.setMeasuring(false);
                            sender.sendMessage(wmhead + ChatColor.RED + "コマンドは間違いました！ Incorrect command!");
                            return true;
                        }
                    } else {
                        measuredist(p, false);
                    }
                    p.sendMessage(wmhead + ChatColor.GREEN + "測定開始 Measuring started");
                } else {
                    p.sendMessage(wmhead + ChatColor.RED + "乗り物に乗ってください！ Please sit in a vehicle!");
                }
            } else {
                DecimalFormat df2 = new DecimalFormat("#.##");
                p.sendMessage(wmhead + ChatColor.RED + "測定終了 Measuring ended");
                p.sendMessage(wmhead + ChatColor.YELLOW + "総走行距離 Total distance: " + df2.format(user.getMeasuretotaldist()) + " m");
                p.sendMessage(wmhead + ChatColor.YELLOW + "総走行時間 Total time: " + tickToTimeFormatter(user.getMeasuretotaltime()));
                user.setMeasuring(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ta = new ArrayList<>();
        List<String> result = new ArrayList<>();
        int arglength = args.length;
        if (arglength == 1) {
            ta.add("marker");
            for (String a : ta) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(a);
                }
            }
            return result;
        } else if (arglength > 1) {
            result.add("");
            return result;
        }
        return null;
    }
}