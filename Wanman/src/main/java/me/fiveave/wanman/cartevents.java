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
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.text.DecimalFormat;
import java.util.Objects;

import static me.fiveave.wanman.main.*;
import static me.fiveave.wanman.wanmanuser.initWanmanuser;

public class cartevents implements Listener {

    public static void measuredist(Player p, Boolean marker) {
        double dist;
        initWanmanuser(p);
        wanmanuser user = wmuser.get(p);
        if (p.isInsideVehicle() && user.isMeasuring()) {
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
            user.setMeasuretotaltime(user.getMeasuretotaltime() + 1);
            user.setMeasuretotaldist(user.getMeasuretotaldist() + dist);
            int intdist = (int) user.getMeasuretotaldist();
            if (marker && Math.floorMod(intdist, 100) == 0) {
                Block blk = Objects.requireNonNull(p.getLocation().getWorld()).getBlockAt(p.getLocation().getBlockX(), p.getLocation().getBlockY() + 2, p.getLocation().getBlockZ());
                blk.setType(Material.OAK_SIGN, false);
                Sign sign = (Sign) blk.getState();
                sign.setLine(1, ChatColor.GOLD + "Wanman " + ChatColor.RED + "Marker");
                sign.setLine(2, ChatColor.BOLD + String.valueOf(intdist) + ChatColor.RESET + " m");
                sign.update();
            }
            DecimalFormat df0 = new DecimalFormat("#");
            DecimalFormat df2 = new DecimalFormat("0.00");
            String actionbarmsg = ChatColor.GOLD + "速度 Speed: " + ChatColor.YELLOW + df0.format(dist * 72) + " km/h"
                    + ChatColor.YELLOW + " | " + ChatColor.GOLD + "距離 Dist: " + ChatColor.YELLOW + df2.format(user.getMeasuretotaldist()) + " m"
                    + " | " + ChatColor.GOLD + "時間 Time: " + ChatColor.YELLOW + tickToTimeFormatter(user.getMeasuretotaltime());
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

    @EventHandler
    public void cartExitEvent(VehicleExitEvent event) {
        if (event.getExited() instanceof Player) {
            Player p = (Player) event.getExited();
            if (!p.isInsideVehicle()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> payEvent(event), 1);
            }
        }
    }

    @EventHandler
    public void cartEnterEvent(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            Player p = (Player) event.getEntered();
            initWanmanuser(p);
            wanmanuser user = wmuser.get(p);
            user.setIncart(true);
        }
    }

    @EventHandler
    public void payEvent(VehicleExitEvent event) {
        if (event.getExited() instanceof Player) {
            Player p = (Player) event.getExited();
            initWanmanuser(p);
            wanmanuser user = wmuser.get(p);
            try {
                if (!p.isInsideVehicle()) {
                    user.setIncart(false);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (!user.isIncart() && user.getTotaldist() != 0) {
                            MinecartGroup mg = MinecartGroupStore.get(event.getVehicle());
                            double multi = 0.07 / 4;
                            // From trainfares (if available)
                            for (Object tname : Objects.requireNonNull(getTF().getConfigurationSection("fares")).getKeys(false)) {
                                multi = getTF().getDouble("fares.default.multiplier");
                                String tname2 = tname.toString();
                                faretable.read("default", 0);
                                try {
                                    if (mg.getProperties().getDisplayName().contains(tname2)) {
                                        multi = getTF().getDouble("fares." + tname2 + ".multiplier");
                                        faretable.read(tname2, 0);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            DecimalFormat df2 = new DecimalFormat("#.##");
                            double km2 = user.getTotaldist() * 0.001;
                            int km = (int) (km2 + 1) - 1 == km2 ? (int) km2 : (int) (km2 + 1);
                            // Read fare table
                            if (user.getTotaldist() > 0) {
                                double fare;
                                if (km > ft.size()) {
                                    fare = ft.get(ft.size() - 1) * multi;
                                } else {
                                    fare = (ft.get(km)) * multi;
                                }
                                Objects.requireNonNull(p.getPlayer()).sendMessage(wmhead + ChatColor.YELLOW + "運賃は $" + df2.format(fare) + " です。\n" + wmhead + "Fare: $" + df2.format(fare));
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco take " + p.getName() + " " + df2.format(fare));
                                user.setTotaldist(0);
                            }
                        }
                    }, 1);
                }
            } catch (NumberFormatException e) {
                p.sendMessage(wmhead + ChatColor.RED + "数字フォーマットエラーが発生しました。A number format exception occurred.");
                e.printStackTrace();
            } catch (CommandException e) {
                p.sendMessage(wmhead + ChatColor.RED + "コマンドエラーが発生しました。A command exception occurred.");
                e.printStackTrace();
            }
        }
    }
}
