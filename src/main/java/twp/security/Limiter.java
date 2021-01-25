package twp.security;

import arc.Core;
import arc.util.*;
import arc.util.Timer;
import static mindustry.net.Administration.*;
import mindustry.world.Tile;
import twp.*;
import twp.commands.RankSetter;
import twp.database.PD;
import twp.database.enums.Perm;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Player;
import twp.tools.Logging;

import java.util.*;

import static twp.Main.*;
import static twp.Global.config;
import static mindustry.Vars.netServer;

// Limiter restricts player actions and manages instance of LockMap
public class Limiter {
    LockMap map;
    HashMap<String, DoubleClickData> doubleClicks = new HashMap<>();


    public Limiter() {
        // Initializing LockMap on start of a game
        Logging.on(EventType.PlayEvent.class, e -> {
            Action.actions.clear();
            map = new LockMap(Vars.world.width(), Vars.world.height());
        });

        // Cases when lock should reset

        Logging.on(EventType.BlockDestroyEvent.class, e -> map.remove(e.tile));

        Logging.on(EventType.BlockBuildEndEvent.class, e -> {
            if(e.breaking){
                map.setLock(e.tile, 0);
            }
        });

        // This mostly prevents griefers from shooting
        Logging.run(EventType.Trigger.update, () -> db.online.forEachValue((iter) -> {
            PD pd = iter.next();
            if(pd.isInvalid()){
                return;
            }
            if(pd.cannotInteract() && pd.player.p.shooting){
                pd.player.p.unit().kill();
            }
        }));

        Logging.on(EventType.TapEvent.class, e -> {
            DoubleClickData dcd = doubleClicks.get(e.player.uuid());
            if (dcd == null || !dcd.Equal(e.tile)) {
                doubleClicks.put(e.player.uuid(), new DoubleClickData(e.tile));
                return;
            }

            if (Time.timeSinceMillis(dcd.time) < config.doubleClickSpacing) {
                map.displayInfo(e.tile, e.player);
            }

            doubleClicks.remove(e.player.uuid());
        });

        if(!Main.testMode) {
            registerActionFilter();
            Config.antiSpam.set(false);
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
                Logging.log("player data is missing ewen though player is attempting actions");
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

            if (act.tile == null) {
                return true;
            }

            int top = pd.getHighestPermissionLevel();
            int lock = map.getLock(act.tile);

            if (lock > top) {
                pd.sendServerMessage("admins-permissionTooLow", top, lock);
                return false;
            } else if (act.type != ActionType.breakBlock && pd.hasPermLevel(Perm.high.value)) {
                map.setLock(act.tile, Perm.high.value);
            }

            Action.ResolveResult rr = Action.resolve(act, pd.id);
            if(rr != null && map.addAction(rr)) {
                if(rr.optional != null) Action.add(rr.optional);
                Action.add(rr.main);
                if(act.type != ActionType.breakBlock && act.type != ActionType.placeBlock) {
                    if(pd.actionOverflow()) {
                        RankSetter.terminal.run("", String.valueOf(pd.id), "griefer");
                        Timer.schedule(() -> queue.post(() -> Action.execute(pd.id, config.actionUndoTime + 2000)), 2f);
                    }
                }
            }

            return true;
        });
    }

    static class DoubleClickData {
        long time = Time.millis();
        Tile t;

        DoubleClickData(Tile t) {
            this.t = t;
        }

        boolean Equal(Tile t) {
            return this.t == t;
        }
    }
}