package me.fiveave.wanman;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static java.lang.Integer.parseInt;
import static me.fiveave.wanman.main.wmhead;
import static me.fiveave.wanman.main.wmuser;
import static me.fiveave.wanman.wanmanuser.initWanmanuser;

public class adddistsign extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("adddist");
    }

    @Override
    public void execute(SignActionEvent cartevent) {
        if (cartevent.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && cartevent.hasRailedMember() && cartevent.isPowered()) {
            cartevent.getMembers().forEach((cart) -> {
                // For each passenger on cart
                CommonEntity<?> cart2 = cart.getEntity();
                cart2.getPassengers().forEach((cartobj) -> {
                    Player p = (Player) cartobj;
                    // Decimal format
                    String[] l3 = cartevent.getLine(2).split(" ");
                    initWanmanuser(p);
                    wanmanuser user = wmuser.get(p);
                    int newdist = user.getTotaldist() + Integer.parseInt(l3[0]);
                    boolean resetpendingtd = true;
                    if (l3.length > 1) {
                        String transtag = l3[1];
                        if (transtag.equals(user.getTranstag())) {
                            // Put pending transfer distance into new totaldist (to continue distance calculation)
                            // Set confirmed transfer distance
                            int pendtransdist = user.getPendingtransdist();
                            user.setConfirmedtransdist(pendtransdist);
                            newdist += pendtransdist;
                            resetpendingtd = false;
                            p.sendMessage(wmhead + ChatColor.GREEN + "乗り換え距離が加算されました。\n" + wmhead + ChatColor.GREEN + "Transfer distance has been added.");
                        }
                    }
                    // If tag not match then reset distance and tag
                    if (resetpendingtd) {
                        user.setPendingtransdist(0);
                        user.setTranstag(null);
                        p.sendMessage(wmhead + ChatColor.RED + "乗り換え距離が加算されませんでした。\n" + wmhead + ChatColor.RED + "Transfer distance has not been added.");
                    }
                    user.setTotaldist(newdist);
                });
            });
        }
    }

    @Override
    public boolean build(SignChangeActionEvent e) {
        try {
            String[] l3 = e.getLine(2).split(" ");
            parseInt(l3[0]);
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