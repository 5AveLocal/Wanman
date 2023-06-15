package me.fiveave.wanman;

import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

import static me.fiveave.wanman.cartevents.getTF;
import static me.fiveave.wanman.main.*;

public class faretable {
    static String faretabletext = "\n";

    public static void read(String s, int bt) {
        // Clear faretable in case of error
        ft.clear();
        faretabletext = wmhead + "運賃表 Fare table:\n" + String.format("%-7s %-7s %-7s\n%-8s %-8s %-8s\n", ChatColor.GREEN + "から", ChatColor.RED + "まで", ChatColor.YELLOW + "運賃", ChatColor.GREEN + "From", ChatColor.RED + "To", ChatColor.YELLOW + "Fare");
        String line;
        try {
            String name = getTF().getString("fares." + s + ".faretable");
            File file = new File(plugin.getDataFolder(), name + ".csv");
            if (!file.exists()) {
                plugin.saveResource(file.getName(), false);
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] sinfo = line.split(",");
                int[] info = new int[sinfo.length];
                for (int i = 0; i <= sinfo.length - 1; i++) {
                    info[i] = Integer.parseInt(sinfo[i]);
                }
                if (bt == 0) {
                    fta(info[0], info[1], info[2]);
                } else if (bt == 1) {
                    double multi = getTF().getDouble("fares." + s + ".multiplier");
                    DecimalFormat df2 = new DecimalFormat("0.00");
                    faretabletext = String.format("%s%s\n", faretabletext, String.format("%s %s %s", ChatColor.GREEN + "" + String.format("%05d", info[0]), ChatColor.RED + "" + String.format("%05d", info[1]), ChatColor.YELLOW + "" + df2.format(multi * info[2])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ft.clear();
        }
    }
}
