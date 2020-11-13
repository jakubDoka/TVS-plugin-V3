package twp.security;

import arc.util.Time;
import twp.database.RankType;
import twp.database.Raw;
import mindustry.net.Administration;
import mindustry.world.Tile;

import java.util.ArrayList;

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

    void AddAction(Tile t, ActionInf inf) {
        map[t.y][t.x].actions.add(inf);
        if(map[t.y][t.x].actions.size() > 5) {
            map[t.y][t.x].actions.remove(0);
        }
    }

    String getActions(Tile t) {
        TileInf ti = map[t.y][t.x];
        StringBuilder sb = new StringBuilder();
        for(ActionInf ai : ti.actions) {
            Raw raw = db.handler.getDoc(ai.id);
            sb.append(db.online.containsKey(raw.getUuid()) ? "[green]" : "[gray]");
            sb.append("[").append(raw.getRank(RankType.rank).color).append("]").append(raw.getName());
            sb.append(" [](").append(raw.getId()).append(") ");
            sb.append(Time.timeSinceMillis(ai.time) / 1000 / 60).append("min ago");
            sb.append("[]\n");
        }
        return sb.substring(0, sb.length() - 2);
    }

    static class TileInf {
        int lock;
        ArrayList<ActionInf> actions = new ArrayList<>();
    }

    static class ActionInf {
        long id, time = Time.millis();
        Administration.ActionType type;

        ActionInf(long id, Administration.ActionType type) {
            this.id = id;
            this.type = type;
        }
    }
}
