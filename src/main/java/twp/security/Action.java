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

    public static Action resolve(PlayerAction act, long id) {
        Action a = new Action(act.type, id, act.tile) {
            @Override
            public void undo() {}
        };

        switch (act.type) {
            case configure:
                return new Config(act.tile.build.config(), a);
            case rotate:
                return new Rotation(act.tile.build.rotation(), a);
            case breakBlock:
                return new Break(act.tile.block(), act.tile.build.config(), act.tile.build.rotation(), act.player.team(), a);
            case placeBlock:
                return new Place(act.block, act.player.team(), a);
        }

        return null;
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
        public boolean insert(Action value) {
            ActionStack a = computeIfAbsent(value.type, k -> new ActionStack());
            if (!a.isEmpty() ) {
                if (a.first().id != value.id) {
                    // When block changes or is removed all other action records are invalidated
                    switch (value.type) {
                        case breakBlock:
                        case placeBlock:
                            erase();
                    }
                } else if (a.first().type == value.type) {
                    switch (value.type) {
                        case breakBlock:
                        case placeBlock:
                            if (value.b.name.startsWith("build") || value.b == a.first().b) {
                                return false;
                            }
                    }
                }
            }
            a.insert(0, value);
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
