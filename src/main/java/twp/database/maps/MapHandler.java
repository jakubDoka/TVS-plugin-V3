package twp.database.maps;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.maps.Map;
import org.bson.Document;
import twp.Main;
import twp.database.core.Handler;
import twp.database.enums.Stat;
import twp.tools.Logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class MapHandler extends Handler {
    public static String mapFolder = "config/maps/";

    public MapHandler(MongoCollection<Document> data, MongoCollection<Document> counter) {
        super(data, counter);
        Logging.on(EventType.GameOverEvent.class, (e)-> {
            if (validateMaps()) {
                return;
            }

            Log.info("Server was closed due to presence of invalid maps.");
            Vars.net.dispose();
            Core.app.exit();
        });

        data.createIndex(Indexes.descending("fileName"));
        if (!validateMaps()) {
            Log.info("Server wos closed due to some of maps being invalid, rename them or use /maps update <fileName> in case you tiring to update them.");
            Core.app.exit();
        }
    }

    public boolean validateMaps() {
        if (Main.testMode) {
            return true;
        }
        boolean valid = true;
        for(Map m : Vars.maps.customMaps()) {
            MapData md = getMap(m.file.name());
            if (md == null) {
                makeNewMapData(m);
                continue;
            }
            try {
                byte[] dt = Files.readAllBytes(Paths.get(m.file.absolutePath()));
                if(!Arrays.equals(dt, md.GetData())) {
                    Log.info("Map file with name " + m.file.name() + " is duplicate.");
                    valid = false;
                }
            } catch (IOException e) {
                Log.info("Unable to read a map file, strange.");
            }

        }

        return valid;
    }

    public MapData getMap(long id) {
        return MapData.getNew(data.find(idFilter(id)).first());
    }

    public MapData getMap(String name) {
        return  MapData.getNew(data.find(Filters.eq("fileName", name)).first());
    }

    public MapData FindMap(Map map) {
        return null;
    }

    // creates account with all settings enabled
    // newcomer rank and sets bord date
    public MapData makeNewMapData(Map map){
        long id = newId();
        data.insertOne(new Document("_id", id));
        setStat(id, Stat.age, Time.millis());
        set(id, "fileName", map.file.name());
        setData(id, map);
        return getMap(id);
    }

    void setData(long id, Map map) {
        try {
            set(id, "data", Files.readAllBytes(Paths.get(map.file.absolutePath())));
        } catch (IOException e) {
            Logging.log(e);
            Logging.log("unable to cache a map into a database");
        }
    }

    public void withdrawMap(long id, String dest) throws IOException {
        MapData md = getMap(id);
        if(md == null) {
            Logging.log("calling withdrawMap on map that does not exist");
            return;
        }

        dest = Paths.get(dest, md.getFileName()).toString();

        byte[] dt = md.GetData();

        File f = new File(dest);
        if (!f.createNewFile()) {
            throw new IOException("withdrawing already withdrawn map");
        }
        FileOutputStream fos = new FileOutputStream(dest);
        fos.write(dt);
        fos.close();
    }

    public void hideMap(long id) throws IOException {
        MapData md = getMap(id);
        if(md == null) {
            Logging.log("calling hideMap on map that does not exist");
            return;
        }

        if (!new File(Paths.get(mapFolder, md.getFileName()).toString()).delete()) {
            throw new IOException("map file does not exist " + Paths.get(mapFolder, md.getFileName()).toString());
        }
    }

    public void deleteMap(long id) {
        data.deleteOne(idFilter(id));
    }

    public boolean update(Map map) {
        MapData md = getMap(map.file.name());
        if (md == null) {
            return false;
        }
        setData(md.getId(), map);
        return true;
    }
}
