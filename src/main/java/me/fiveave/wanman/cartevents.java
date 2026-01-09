package me.fiveave.wanman;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.text.DecimalFormat;
import java.util.Objects;

import static me.fiveave.wanman.distmeasurer.getFareFromTable;
import static me.fiveave.wanman.distmeasurer.getTF;
import static me.fiveave.wanman.main.*;
import static me.fiveave.wanman.wanmanuser.initWanmanuser;

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
            initWanmanuser(p);
            wanmanuser user = wmuser.get(p);
            user.setIncart(true);
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
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
                            // Read fare table
                            if (user.getTotaldist() > 0) {
                                double fare;
                                double dist = user.getTotaldist();
                                fare = getFareFromTable(dist, multi);
                                double conftransdist = user.getConfirmedtransdist();
                                // Transfer discount
                                if (conftransdist > 0) {
                                    fare -= getFareFromTable(conftransdist, multi);
                                    user.setConfirmedtransdist(0);
                                    user.setTranstag(null);
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
