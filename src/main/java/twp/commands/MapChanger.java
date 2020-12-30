package twp.commands;

import arc.math.Mathf;
import mindustry.game.Gamemode;
import mindustry.gen.Call;
import mindustry.maps.Map;
import mindustry.net.WorldReloader;
import twp.database.enums.Perm;
import twp.database.maps.MapData;
import twp.democracy.Voting;
import twp.tools.Logging;
import twp.tools.Text;

import static arc.Core.app;
import static mindustry.Vars.*;
import static twp.Main.*;

public class MapChanger extends Command {

    Voting main = new Voting(this, "main", 5, 1) {
        {
            protection = Perm.change;
        }
    };

    public MapChanger() {
        name = "map";
        argStruct = "<change/list> [id/page]";
        description = "allows changing and rating maps, also can show list of maps";
    }

    @Override
    public void run(String id, String... args) {
        switch (args[0]) {
            case "list":
                int page = 1;
                if (args.length > 1) {
                    if(isNotInteger(args, 1)) {
                        return;
                    }
                    page = Integer.parseInt(args[1]);
                }

                if(caller == null) {
                    Logging.info("custom", Text.formPage(db.maps.listMaps(), page, "maps", 30));
                } else {
                    Call.infoMessage(caller.player.p.con, Text.formPage(db.maps.listMaps(), page, "maps", 20));
                }

                result = Result.none;
                return;
            case "change":
                if(checkArgCount(args.length, 2)) {
                    return;
                }

                if(isNotInteger(args, 1)) {
                    return;
                }

                long mid = Long.parseLong(args[1]);
                MapData md = db.maps.getMap(mid);
                if(md == null) {
                    result = Result.notFound;
                    return;
                }

                Map map = maps.customMaps().find(m -> m.file.name().equals(md.getFileName()));
                if(map == null) {
                    result = Result.invalidRequest;
                    return;
                }

                app.post(() -> {
                    if (caller != null) {
                        main.pushSession(caller, s -> changeMap(map), map.name());
                    } else {
                        changeMap(map);
                    }
                });

                result = Result.none;
                return;
            case "rate":
                if(testMode) {
                    result = Result.rateSuccess;
                    return;
                }

                MapData md1 = db.maps.getMap(state.map.file.name());
                if(md1 == null) {
                    Logging.log(new RuntimeException("Current map is not present in database"));
                    result = Result.bug;
                    return;
                }

                if(isNotInteger(args, 1)) {
                    return;
                }

                int rating = Integer.parseInt(args[1]);
                rating = Mathf.clamp(rating, 0, 10);

                if(caller != null) {
                    db.maps.addRating(md1.getId(), caller.player.uuid, rating);
                } else {
                    db.maps.addRating(md1.getId(), "owner", rating);
                }

                setArg(rating);
                result = Result.rateSuccess;
                return;
            default:
                result = Result.wrongOption;
        }
    }

    void changeMap(Map map) {
        if(testMode) {
            return;
        }

        WorldReloader reloader = new WorldReloader();

        reloader.begin();

        Gamemode mode = map.rules().mode();

        world.loadMap(map, map.applyRules(mode));

        state.rules = state.map.applyRules(mode);
        logic.play();

        reloader.end();
    }

    public static MapChanger game = new MapChanger();
    public static MapChanger terminal = new MapChanger();
}
