package me.fiveave.wanman;

import com.bergerkiller.bukkit.common.entity.CommonEntity;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static me.fiveave.wanman.main.WM_HEAD;
import static me.fiveave.wanman.main.wmuser;
import static me.fiveave.wanman.wanmanuser.initWanmanuser;

public class wmtranssign extends SignAction {

    @Override
    public boolean match(SignActionEvent info) {
        return info.isType("wmtrans");
    }

    @Override
    public void execute(SignActionEvent cartevent) {
        if (cartevent.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON) && cartevent.hasRailedMember() && cartevent.isPowered()) {
            cartevent.getMembers().forEach(cart -> {
                // For each passenger on cart
                CommonEntity<?> cart2 = cart.getEntity();
                cart2.getPassengers().forEach(cartobj -> {
                    Player p = (Player) cartobj;
                    // Decimal format
                    initWanmanuser(p);
                    wanmanuser user = wmuser.get(p);
                    String tag = cartevent.getLine(2);
                    user.setTranstag(tag);
                    user.setPendingtransdist(user.getTotaldist());
                    p.sendMessage(WM_HEAD + ChatColor.YELLOW + "乗り換え割引 Transfer Discount" + ChatColor.GREEN + " + " + tag);
                });
            });
        }
    }

    @Override
    public boolean build(SignChangeActionEvent e) {
        try {
            if (e.getLine(2).isEmpty()) {
                e.getPlayer().sendMessage(ChatColor.RED + "The tag is not valid!");
                e.setCancelled(true);
                return true;
            }
            SignBuildOptions opt = SignBuildOptions.create().setName(ChatColor.GOLD + "Transfer Setter");
            opt.setDescription("Set transfer tag to players on train");
            return opt.handle(e.getPlayer());
        } catch (Exception exception) {
            e.getPlayer().sendMessage(ChatColor.RED + "Arguments are not valid!");
            e.setCancelled(true);
        }
        return true;
    }
}