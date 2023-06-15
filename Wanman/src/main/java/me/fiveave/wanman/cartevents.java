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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.text.DecimalFormat;
import java.util.Objects;

import static me.fiveave.wanman.main.*;

public class cartevents implements Listener {

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
            incart.put(p, true);
            totaldist.putIfAbsent(p, 0);
            Entity selcart = p.getVehicle();
            MinecartGroup mg = MinecartGroupStore.get(selcart);
            if (selcart instanceof Minecart) {
                cart.put(p, mg);
            }
        }
    }

    @EventHandler
    public void payEvent(VehicleExitEvent event) {
        if (event.getExited() instanceof Player) {
            Player p = (Player) event.getExited();
            totaldist.putIfAbsent(p, 0);
            incart.putIfAbsent(p, false);
            try {
                if (!p.isInsideVehicle()) {
                    incart.put(p, false);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (!incart.get(p) && !totaldist.get(p).equals(0)) {
                            MinecartGroup mg = cart.get(p);
                            double multi = 0.07 / 4;
                            // From trainfares (if available)
                            for (Object tname : Objects.requireNonNull(getTF().getConfigurationSection("fares")).getKeys(false)) {
                                multi = getTF().getDouble("fares.default.multiplier");
                                String tname2 = tname.toString();
                                faretable.read("default", 0);
                                if (mg.getProperties().getDisplayName().contains(tname2)) {
                                    multi = getTF().getDouble("fares." + tname2 + ".multiplier");
                                    faretable.read(tname2, 0);
                                }
                            }
                            cart.remove(p);
                            DecimalFormat df2 = new DecimalFormat("#.##");
                            double km2 = totaldist.get(p) * 0.001;
                            int km = (int) (km2 + 1) - 1 == km2 ? (int) km2 : (int) (km2 + 1);
                            // Read fare table
                            if (totaldist.get(p) > 0) {
                                double fare;
                                if (km > ft.size()) {
                                    fare = ft.get(ft.size() - 1) * multi;
                                } else {
                                    fare = (ft.get(km)) * multi;
                                }
                                Objects.requireNonNull(p.getPlayer()).sendMessage(wmhead + ChatColor.YELLOW + "運賃は $" + df2.format(fare) + " です。\n" + wmhead + "Fare: $" + df2.format(fare));
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco take " + p.getName() + " " + df2.format(fare));
                                totaldist.put(p, 0);
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

    public static void measuredist(Player p, Boolean marker) {
        double dist;
        measuring.putIfAbsent(p, false);
        if (p.isInsideVehicle() && measuring.get(p)) {
            measuretotaldist.putIfAbsent(p, 0.0);
            if (p.getVehicle() instanceof Minecart) {
                MinecartGroup mg = MinecartGroupStore.get(p.getVehicle());
                TrainProperties tprop = mg.getProperties();
                dist = Math.min(tprop.getSpeedLimit(), mg.getAverageForce());
            } else {
                double locx = p.getLocation().getX();
                double locz = p.getLocation().getZ();
                lastx.putIfAbsent(p, locx);
                lastz.putIfAbsent(p, locz);
                dist = Math.hypot(locx - lastx.get(p), locz - lastz.get(p));
                lastx.put(p, locx);
                lastz.put(p, locz);
            }
            measuretotaldist.put(p, measuretotaldist.get(p) + dist);
            int intdist = (int) measuretotaldist.get(p).doubleValue();
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
            String actionbarmsg = ChatColor.GOLD + "速度 Speed: " + ChatColor.YELLOW + df0.format(dist * 72) + " km/h" + ChatColor.YELLOW + " | " + ChatColor.GOLD + "距離 Dist: " + ChatColor.YELLOW + df2.format(measuretotaldist.get(p)) + " m";
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbarmsg));

            Bukkit.getScheduler().runTaskLater(plugin, () -> measuredist(p, marker), 1);
        } else if (!p.isInsideVehicle()) {
            Bukkit.dispatchCommand(p, "wanmandist");
        }
    }

    static FileConfiguration getTF() {
        return trainfares.dataconfig;
    }
}
