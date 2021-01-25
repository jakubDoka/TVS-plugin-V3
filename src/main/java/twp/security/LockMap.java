package twp.security;

import arc.util.Log;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.net.Administration.*;
import mindustry.world.*;
import twp.*;
import twp.database.*;
import twp.database.enums.*;

import static twp.Main.*;

import java.util.*;

// LockMap saves data about player actions, It is rebuild on start of each game to fit the map
public class LockMap {
    TileInf[][] map;

    LockMap(int width, int height) {
        map = new TileInf[height][width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                map[y][x] = new TileInf();
            }
        }
    }

    int getLock(Tile t) {
        return map[t.y][t.x].lock;
    }

    void setLock(Tile t, int level) {
        map[t.y][t.x].lock = level;
    }

    void displayInfo(Tile t, Player p) {
        Call.label(p.con, map[t.y][t.x].format(), 10, t.worldx(), t.worldy());
    }

    public boolean addAction(Action.ResolveResult rr){
        return map[rr.main.t.y][rr.main.t.x].addAction(rr);
    }

    public void remove(Tile t) {
        setLock(t, 0);
        map[t.y][t.x].actionTile.erase();
    }

    static class TileInf {
        int lock;
        ArrayList<ActionInf> actions = new ArrayList<>();
        Action.ActionTile actionTile = new Action.ActionTile();

        String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("Lock: ").append(lock).append("\n");
            for (ActionInf ai : actions) {
                sb.append(ai.format()).append("\n");
            }
            return sb.substring(0, sb.length()-1);
        }

        public boolean addAction(Action.ResolveResult rr){
            boolean res = actionTile.insert(rr);
            switch (rr.main.type) {
                case breakBlock:
                case placeBlock:
                    if(actions.size() != 0 && actions.get(0).type == rr.main.type && actions.get(0).id == rr.main.id) {
                        return res;
                    }
            }

            actions.add(0, new ActionInf(rr.main.id, rr.main.type));
            if (actions.size() > Global.config.actionMemorySize) {
                actions.remove(actions.size()-1);
            }
            return res;
        }
    }

    static class ActionInf {
        long id;
        Administration.ActionType type;

        ActionInf(long id, Administration.ActionType type) {
            this.id = id;
            this.type = type;
        }

        String format() {
            Account pd = db.handler.getAccount(id);
            if (pd == null) {
                return "hello there, i em corrupted";
            }
            return id + "-" + pd.getName() + "-" + pd.getRank(RankType.rank).getSuffix() + "-" + type.name();
        }
    }
}
