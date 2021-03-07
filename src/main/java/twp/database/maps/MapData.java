package main.java.twp.database.maps;

import mindustry.maps.Map;
import org.bson.Document;
import org.bson.types.Binary;
import main.java.twp.database.core.Raw;
import main.java.twp.tools.Logging;

import java.io.File;
import java.nio.file.Paths;

// Map document abstraction
public class MapData extends Raw {
    public static MapData getNew(Document data) {
        if (data == null) return null;
        return new MapData(data);
    }

    public MapData(Document data) {
        this.data = data;
    }

    public byte[] GetData() {
        return ((Binary) data.get("data")).getData();
    }

    public String getFileName() {
        return data.getString("fileName");
    }

    public boolean isEnabled() {
        return new File(Paths.get(MapHandler.mapFolder, getFileName()).toString()).exists();
    }

    public String getRating() {
        Document ratings = (Document) data.get("rating");
        if(ratings == null || ratings.size() == 0) {
            return "none";
        }

        int total = 0;
        for(Object r : ratings.values()) {
            if(r instanceof Integer) {
                total += (Integer) r;
            } else {
                Logging.log(new RuntimeException("illegal data in map ratings"));
            }
        }

        return String.valueOf(total/ratings.size());
    }

    public String summarize(Map map) {
        return String.format("%d - %s - (%s/10)", getId(), map.name(), getRating());
    }
}
