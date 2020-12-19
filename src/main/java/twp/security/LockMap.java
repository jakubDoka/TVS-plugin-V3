package twp.security;

import arc.util.*;
import mindustry.gen.*;
import mindustry.net.*;
import mindustry.net.Administration.*;
import mindustry.world.*;
import twp.*;
import twp.database.*;
import twp.database.enums.*;

import java.util.*;

import static twp.Main.db;

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
        Call.label(p.con, map[t.x][t.y].format(), 10, t.worldx(), t.worldy());
    }

    public void addAction(Tile t, long id, ActionType type){
        map[t.x][t.y].addAction(id, type);
    }

    static class TileInf {
        int lock;
        ArrayList<ActionInf> actions = new ArrayList<>();

        String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("Lock: ").append(lock).append("\n");
            for (ActionInf ai : actions) {
                sb.append(ai.format()).append("\n");
            }
            return sb.substring(0, sb.length()-1);
        }

        public void addAction(long id, ActionType type){
            if(actions.size() != 0 && actions.get(0).type == type && actions.get(0).id == id) {
                return;
            }
            actions.add(0, new ActionInf(id, type));
            if (actions.size() > Global.config.actionMemorySize) {
                actions.remove(actions.size()-1);
            }
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
