package twp.security;

import arc.util.*;
import mindustry.net.Administration;
import mindustry.world.Tile;
import twp.*;
import twp.database.PD;
import twp.database.enums.Perm;
import twp.database.enums.Setting;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Player;
import twp.tools.Logging;

import java.util.*;

import static twp.Main.db;
import static twp.Main.ranks;
import static mindustry.Vars.netServer;

public class Limiter {
    LockMap map;
    HashMap<String, DoubleClickData> doubleClicks = new HashMap<>();


    public Limiter() {
        // Initializing LockMap on start of a game
        Logging.on(EventType.PlayEvent.class, e -> map = new LockMap(Vars.world.width(), Vars.world.height()));

        // Cases when lock should reset

        Logging.on(EventType.BlockDestroyEvent.class, e -> map.setLock(e.tile,0));

        Logging.on(EventType.BlockBuildEndEvent.class, e -> {
            if(e.breaking){
                map.setLock(e.tile, 0);
            }
        });

        // This mostly prevents griefers from shooting
        Logging.run(EventType.Trigger.update, () -> {
            db.online.forEachValue((iter) -> {
                PD pd = iter.next();
                if(pd.isInvalid()){
                    return;
                }
                if(pd.cannotInteract() && pd.player.p.shooting){
                    pd.player.p.unit().kill();
                }
            });
        });

        Logging.on(EventType.TapEvent.class, e -> {
            DoubleClickData dcd = doubleClicks.get(e.player.uuid());
            if (dcd == null || !dcd.Equal(e.tile)) {
                doubleClicks.put(e.player.uuid(), new DoubleClickData(e.tile));
                return;
            }

            if (Time.timeSinceMillis(dcd.time) < Global.config.doubleClickSpacing) {
                map.displayInfo(e.tile, e.player);
            }

            doubleClicks.remove(e.player.uuid());
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
            } else if (act.type != Administration.ActionType.breakBlock && pd.hasPermLevel(Perm.high.value)) {
                map.setLock(act.tile, /*db.hasEnabled(pd.id, Setting.lock) ? top :*/ Perm.high.value);
            }
            map.addAction(act.tile, pd.id, act.type);



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

/*
Get chills = mat zimnicu
Diphtheria = zaskrt
-Pertussis = cierny kasel
-Intestines = creva
-Lukewarm = vlazny
Polio = decka obrna
Procrastination = odkladanie
Ointment = nasticka
Medical Record = lekarska sprava
Blisters = pluzgiere
Outweigh = prevazovat
Above = nad
Rubella = ruzienka
-Soothe = upokojit
Compresses = obklady
-Splint = dlaha
Subside = ustupit
Swell = nafukat
-Tonsils = mandle
Well-being = zdravie
 */

/*
drama -> dejstva -> vystupy -> dialogy -> repliky
 */