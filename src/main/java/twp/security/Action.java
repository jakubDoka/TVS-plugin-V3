package twp.security;

import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Timer;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.net.Administration.ActionType;
import mindustry.net.Administration.PlayerAction;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import twp.Global;
import twp.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Action {
    static HashMap<Long, ActionStack> actions = new HashMap<>();
    static ResolveResult rr = new ResolveResult();
    static Action ac = new Action(null, 0, null) {
        @Override
        public void undo() { }
    };

    ActionType type;
    Tile t;
    Block b;
    Object config;
    Team team;

    int rotation;
    long id, age = Time.millis();
    boolean outdated;

    public Action(ActionType type, long id, Tile t) {
        this.type = type;
        this.id = id;
        this.t = t;
    }

    public static ResolveResult resolve(PlayerAction act, long id) {
        ac.type = act.type;
        ac.id = id;
        ac.t = act.tile;

        rr.optional = null;
        rr.main = null;

        switch (act.type) {
            case configure:
                rr.main = new Config(act.tile.build.config(), ac);
                break;
            case rotate:
                rr.main = new Rotation(act.tile.build.rotation(), ac);
                break;
            case placeBlock:
                rr.main = new Place(act.block, act.player.team(), ac);
            case breakBlock:
                Block b = act.tile.block();
                if (b.name.startsWith("build") || b.name.startsWith("core") ||
                        act.block.name.startsWith("build") || act.block.name.startsWith("core")) {
                    return null;
                }

                if(b == Blocks.air || act.tile.build == null) {
                    break;
                }

                Action action = new Break(b, act.tile.build.config(), act.tile.build.rotation(), act.player.team(), ac);

                if (rr.main == null) {
                    rr.main = action;
                } else {
                    rr.optional = action;
                }
                break;
            default:
                return null;
        }

        return rr;
    }

    public static void add(Action action) {
        ActionStack as = actions.computeIfAbsent(action.id, id -> new ActionStack());
        as.insert(0, action);
    }

    public static void execute(long id, int amount) {
        if(actions.containsKey(id)) {
            actions.get(id).execute(amount);
        }
    }

    public static void execute(long id, long time) {
        if(actions.containsKey(id)) {
            actions.get(id).execute(time);
        }
    }

    public void remove() {
        outdated = true;
    }

    public abstract void undo();

    public static class ResolveResult {
        Action main, optional;
    }

    public static class Config extends Action {
        public Config(Object config, Action a) {
            super(a.type, a.id, a.t);
            this.config = config;
        }

        @Override
        public void undo() {
            t.build.configureAny(config);
        }
    }

    public static class Rotation extends Action {
        public Rotation(int rotation, Action a) {
            super(a.type, a.id, a.t);
            this.rotation = rotation;
        }

        @Override
        public void undo() {
            t.build.rotation(rotation);
        }
    }

    public static class Place extends Action {
        public Place(Block b, Team team, Action a) {
            super(a.type, a.id, a.t);
            this.b = b;
            this.team = team;
        }

        @Override
        public void undo() {
            if (team.core() == null) return;
            team.core().items.add(new Seq<>(b.requirements));
            Call.deconstructFinish(t, b, null);
        }
    }

    public static class Break extends Action {
        public Break(Block b, Object config, int rotation, Team team, Action a) {
            super(a.type, a.id, a.t);
            this.b = b;
            this.config = config;
            this.rotation = rotation;
            this.team = team;
        }

        @Override
        public void undo() {
            if (team.core() == null) return;
            ConstructBlock.constructed(t, b, null, (byte)rotation, team, config);
        }
    }

    public static class ActionTile extends HashMap<ActionType, ActionStack> {
        long id;

        public boolean insert(ResolveResult rr) {
            ActionStack as = computeIfAbsent(rr.main.type, k -> new ActionStack());

            if(id != rr.main.id) {
                erase();
            } else if (!as.isEmpty() && as.first().type == rr.main.type) {
                switch (rr.main.type) {
                    case breakBlock:
                    case placeBlock:
                        if (rr.main.b == as.first().b) {
                            return false;
                        }
                }
            }

            id = rr.main.id;
            if(rr.optional != null) {
                ActionStack as1 = computeIfAbsent(rr.optional.type, k -> new ActionStack());
                as1.insert(0, rr.optional);
            }
            as.insert(0, rr.main);
            return true;
        }

        public void erase() {
            forEach((at, ac) -> {
                ac.forEach(Action::remove);
                ac.clear();
            });
        }
    }

    public static class ActionStack extends Seq<Action> {
        public void execute(int amount) {
            Iterator<Action> iter = iterator();
            for(int i = 0; i < amount && iter.hasNext(); i++) {
                Action a = iter.next();
                if (!a.outdated) a.undo();
                else i--;
                iter.remove();
            }
        }

        public void execute(long time) {
            Iterator<Action> iter = iterator();
            while (iter.hasNext()){
                Action a = iter.next();
                if(Time.timeSinceMillis(a.age) > time) {
                    break;
                }
                if (!a.outdated) a.undo();
                iter.remove();
            }
        }
    }
}
