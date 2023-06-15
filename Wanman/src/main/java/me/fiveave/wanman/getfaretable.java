package me.fiveave.wanman;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.fiveave.wanman.cartevents.getTF;
import static me.fiveave.wanman.faretable.faretabletext;
import static me.fiveave.wanman.main.cart;

public class getfaretable implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if (p.getVehicle() instanceof Minecart) {
            for (Object tname : Objects.requireNonNull(getTF().getConfigurationSection("fares")).getKeys(false)) {
                String tname2 = tname.toString();
                faretable.read("default", 1);
                MinecartGroup mg = cart.get(p);
                if (mg.getProperties().getDisplayName().contains(tname2)) {
                    faretable.read(tname2, 1);
                }
            }
            p.sendMessage(faretabletext);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ta = new ArrayList<>();
        List<String> result = new ArrayList<>();
        ta.add("");
        for (String a : ta) {
            if (a.toLowerCase().startsWith(args[0].toLowerCase())) {
                result.add(a);
            }
        }
        return result;
    }
}
