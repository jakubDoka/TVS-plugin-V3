package twp.database.maps;

import arc.util.Log;
import arc.util.Time;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import mindustry.maps.Map;
import mindustry.world.Tile;
import org.bson.Document;
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

import static twp.Main.ranks;

public class MapHandler extends Handler {
    public static String mapFolder = "config/maps/";

    public MapHandler(MongoCollection<Document> data, MongoCollection<Document> counter) {
        super(data, counter);
        data.createIndex(Indexes.descending("name"));
    }

    public MapData getMap(long id) {
        return MapData.getNew(data.find(idFilter(id)).first());
    }

    public MapData getMap(String name) {
        return  MapData.getNew(data.find(Filters.eq("name", name)).first());
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
        set(id, "name", map.file.name());
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

        dest = Paths.get(dest, md.getName()).toString();

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

        if (!new File(Paths.get(mapFolder, md.getName()).toString()).delete()) {
            throw new IOException("map file does not exist " + Paths.get(mapFolder, md.getName()).toString());
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
