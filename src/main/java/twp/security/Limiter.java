package twp.security;

import arc.Events;
import mindustry.net.Administration;
import twp.Main;
import twp.database.PD;
import twp.database.Perm;
import twp.database.Setting;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Player;
import twp.tools.Testing;

import static twp.Main.db;
import static twp.Main.ranks;
import static mindustry.Vars.netServer;

public class Limiter {
    LockMap map;


    public Limiter() {
        // Initializing LockMap on start of a game
        Events.on(EventType.PlayEvent.class, e -> map = new LockMap(Vars.world.width(), Vars.world.height()));

        // Cases when lock should reset

        Events.on(EventType.BlockDestroyEvent.class, e -> map.setLock(e.tile,0));

        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if(e.breaking) {
                map.setLock(e.tile, 0);
            }
        });

        // This mostly prevents griefers from shooting
        Events.run(EventType.Trigger.update, () -> {
            db.online.forEachValue((iter) -> {
                PD pd = iter.next();
                if(pd.isInvalid()) {
                    return;
                }
                if(pd.cannotInteract() && pd.player.p.shooting) {
                    pd.player.p.unit().kill();
                }
            });
        });


        if(!Main.testMode) {
            registerActionFilter();
        }
    }

    void registerActionFilter() {
        netServer.admins.addActionFilter( act -> {
            Player player = act.player;
            if(player == null) {
                return true; // Dont forget this true is important
            }

            PD pd = db.online.get(player.uuid());
            if (pd == null) {
                Testing.Log("player data is missing ewen though player is attempting actions");
                return true;
            }

            if (pd.rank == ranks.griefer) {
                pd.sendServerMessage("admins-grieferCannotBuild");
                return false;
            }

            if (pd.paralyzed) {
                pd.sendServerMessage("admins-paralyzedCannotBuild");
                return false;
            }

            int top = pd.getHighestPermissionLevel();
            int lock = map.getLock(act.tile);
            if (lock > top) {
                pd.sendServerMessage("admins-permissionTooLow", top, lock);
                return false;
            } else if (pd.hasPermLevel(Perm.high.value) && act.type == Administration.ActionType.placeBlock) {
                map.setLock(act.tile, db.hasEnabled(pd.id, Setting.lock) ? top : Perm.high.value);
            }

            return true;
        });
    }
}
