package me.fiveave.wanman;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static me.fiveave.wanman.main.*;

public class distmeasurer implements CommandExecutor, TabCompleter {

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
        measuring.putIfAbsent(p, false);
        try {
            if (!measuring.get(p)) {
                if (p.isInsideVehicle()) {
                    measuring.put(p, true);
                    if (args.length == 1) {
                        if (args[0].equals("marker")) {
                            cartevents.measuredist(p, true);
                            p.sendMessage(wmhead + ChatColor.YELLOW + "標識は100メートルごとに配置されます。 Signs are placed every 100 meters.");
                        } else {
                            measuring.put(p, false);
                            sender.sendMessage(wmhead + ChatColor.RED + "コマンドは間違いました！ Incorrect command!");
                            return true;
                        }
                    } else {
                        cartevents.measuredist(p, false);
                    }
                    p.sendMessage(wmhead + ChatColor.GREEN + "測定開始 Measuring started");
                } else {
                    p.sendMessage(wmhead + ChatColor.RED + "乗り物に乗ってください！ Please sit in a vehicle!");
                }
            } else {
                measuretotaldist.putIfAbsent(p, 0.0);
                DecimalFormat df2 = new DecimalFormat("#.##");
                p.sendMessage(wmhead + ChatColor.RED + "測定終了 Measuring ended");
                p.sendMessage(wmhead + ChatColor.YELLOW + "総走行距離 Total distance: " + df2.format(measuretotaldist.get(p)) + " m");
                measuretotaldist.remove(p);
                lastx.remove(p);
                lastz.remove(p);
                measuring.put(p, false);
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