package me.fiveave.wanman;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

import static java.lang.Integer.parseInt;
import static me.fiveave.wanman.main.totaldist;

public class sign extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("adddist");
    }

    @Override
    public void execute(SignActionEvent cartevent) {
        if (cartevent.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && cartevent.hasRailedMember() && cartevent.isPowered()) {
            cartevent.setLevers(true);
            //noinspection rawtypes
            for (MinecartMember cart : cartevent.getMembers()) {
                // For each passenger on cart
                //noinspection rawtypes
                CommonEntity cart2 = cart.getEntity();
                //noinspection rawtypes
                List cartpassengers = cart2.getPassengers();
                for (Object cartobject : cartpassengers) {
                    Player cartplayer = (Player) cartobject;
                    // Decimal format
                    totaldist.putIfAbsent(cartplayer, 0);
                    totaldist.put(cartplayer, (parseInt(cartevent.getLine(2))) + totaldist.get(cartplayer));
                }
            }
        } else if (cartevent.isAction(SignActionType.GROUP_LEAVE, SignActionType.REDSTONE_OFF) && !cartevent.hasRailedMember() && !cartevent.isPowered()) {
            cartevent.setLevers(false);
        }
    }

    @Override
    public boolean build(SignChangeActionEvent e) {
        try {
            parseInt(e.getLine(2));
            SignBuildOptions opt = SignBuildOptions.create().setName(ChatColor.GOLD + "Distance Adder");
            opt.setDescription("Add distance to passengers on train");
            return opt.handle(e.getPlayer());
        } catch (Exception exception) {
            e.getPlayer().sendMessage(ChatColor.RED + "The number is not valid!");
            e.setCancelled(true);
        }
        return true;
    }
}