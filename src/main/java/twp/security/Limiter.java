package twp.security;

import arc.Events;
import twp.database.PD;
import twp.database.Perm;
import twp.database.Setting;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Player;

import static twp.Main.db;
import static twp.Main.ranks;
import static mindustry.Vars.netServer;

public class Limiter {
    LockMap map;

    public String
            grieferActionsForbidden = "you have no permission to do anything, appeal to admins to get rid of your griefer mark",
            paralyzedActionsForbidden = "you are paralyzed, easiest way out is using command [orange]/account new[]" +
            ". In case you have account and you protected it with password use " +
            "[orange]/account <id> <password>[]";


    public Limiter() {
        Events.on(EventType.PlayEvent.class, e -> {
            map = new LockMap(Vars.world.width(), Vars.world.height());
        });

        Events.on(EventType.BlockDestroyEvent.class, e -> map.setLock(e.tile,0));

        Events.run(EventType.Trigger.update, () -> {
            for(PD pd : db.online.values()) {
                if(pd == null) continue;
                if(!pd.isGriefer() && pd.player.p.shooting) {
                    pd.player.p.shooting(false);
                    //p.unit().kill();
                }
            }
        });


        registerActionFilter();
    }

    void registerActionFilter() {
        netServer.admins.addActionFilter( act -> {
            Player player = act.player;
            if(player == null) {
                return true;
            }

            PD pd = db.online.get(player.uuid());
            if (pd == null) {
                new RuntimeException("player data is missing ewenthough player is attempting actions").printStackTrace();
                return true;
            }

            if (pd.rank == ranks.griefer) {
                player.sendMessage(grieferActionsForbidden);
                return false;
            }

            if (pd.paralyzed) {
                player.sendMessage(paralyzedActionsForbidden);
                return false;
            }

            int top = pd.getHighestPermissionLevel();
            if (map.getLock(act.tile) > top) {
                player.sendMessage("your permission level is "+ top +", but to interact with " +
                        "a tile you need at least " + map.getLock(act.tile) + ". Ask admins how to increase it.");
            } else if (pd.hasPermLevel(Perm.high.value)) {
                map.setLock(act.tile, db.hasEnabled(pd.id, Setting.lock) ? top : Perm.high.value);
            }


            return true;
        });
    }
}
