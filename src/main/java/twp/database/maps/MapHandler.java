package twp.database.maps;

import arc.Events;
import arc.util.Log;
import arc.util.OS;
import arc.util.Time;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.maps.Map;
import mindustry.world.Tile;
import org.bson.Document;
import twp.Main;
import twp.database.Account;
import twp.database.core.Handler;
import twp.database.enums.RankType;
import twp.database.enums.Setting;
import twp.database.enums.Stat;
import twp.tools.Testing;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static twp.Main.db;
import static twp.Main.ranks;

public class MapHandler extends Handler {
    public static String mapFolder = "config/maps/";
    public boolean invalid;

    public MapHandler(MongoCollection<Document> data, MongoCollection<Document> counter) {
        super(data, counter);
        Events.on(EventType.GameOverEvent.class, (e)-> {
            Vars.net.closeServer();
            Vars.state.set(GameState.State.menu);
            Log.info("server wos closed due to presence of invalid maps");
            validateMaps();
        });
        data.createIndex(Indexes.descending("fileName"));
        if (!validateMaps()) {
            Log.info("Some of maps are invalid, rename them of use /maps update <fileName> in case you tiring to update them.");
            System.exit(2);
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
                db.maps.makeNewMapData(m);
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
            e.printStackTrace();
            throw new RuntimeException("unable to cache a map into a database");
        }
    }

    public void withdrawMap(long id, String dest) throws IOException {
        MapData md = getMap(id);
        if(md == null) {
            Testing.Log("calling withdrawMap on map that does not exist");
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
            Testing.Log("calling hideMap on map that does not exist");
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