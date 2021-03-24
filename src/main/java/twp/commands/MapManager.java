package twp.commands;

import arc.files.Fi;
import twp.database.maps.MapData;
import twp.database.maps.MapHandler;
import mindustry.Vars;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import static twp.Main.*;
import twp.tools.*;

import java.io.File;
import java.io.IOException;

public class MapManager extends Command {
    public MapManager() {
        name = "maps";
        argStruct = "<enable/disable/add/remove/update> <filePath/id> [...comment]";
        description = "command for managing maps, only for admins";
    }

    @Override
    public void run(String id, String... args) {
        if (!verifier.verify(id)) {
            result = Result.noPerm;
            return;
        }

        if (wrongOption(0, args, "enable disable add remove update")) return;

        if (args[0].equals("add") || args[0].equals("update")) {
            if (!new File(args[1]).exists()) {
                result = Result.notExist;
                return;
            }
            try {
                Map map = MapIO.createMap(new Fi(args[1]), true);
                if (args[0].equals("add")) {
                    if(db.maps.getMap(map.file.name()) != null) {
                        result = Result.alreadyAdded;
                        return;
                    }
                    MapData dt = db.maps.makeNewMapData(map);
                    result = Result.addSuccess;
                    setArg(dt.getId());
                } else {
                    if (db.maps.update(map)) {
                        result = Result.updateSuccess;
                    } else {
                        result = Result.updateFail;
                    }
                }
            } catch (IOException ex) {
                result = Result.invalidFile;
                setArg(ex.getMessage());
                return;
            }

            return;
        }

        if (isNotInteger(args, 1)) {
            return;
        }

        long mid = Long.parseLong(args[1]);

        MapData md = db.maps.getMap(mid);

        if (md == null) {
            result = Result.notFound;
            return;
        }

        switch (args[0]) {
            case "enable":
                if (md.isEnabled()) {
                    result = Result.alreadyEnabled;
                    return;
                }
                try {
                    db.maps.withdrawMap(mid, MapHandler.mapFolder);
                    if ( !testMode) queue.post(()-> Vars.maps.reload()); // just in case
                    break;
                } catch (IOException e) {
                    result = Result.enableFail;
                    setArg(e.getMessage());
                    return;
                }
            case "disable":
                if (!md.isEnabled()) {
                    result = Result.alreadyDisabled;
                    return;
                }
            case "remove":
                try {
                    db.maps.hideMap(mid);
                    if ( !testMode) queue.post(()-> Vars.maps.reload());
                } catch (IOException e) {
                    if (args[0].equals("disable")) Logging.log(e);
                }

                if (args[0].equals("remove")) {
                    db.maps.deleteMap(mid);
                }
                break;
        }
    }

    public static MapManager terminal = new MapManager();
    public static MapManager game = new MapManager(){{verifier = this::isPlayerAdmin;}};
}
