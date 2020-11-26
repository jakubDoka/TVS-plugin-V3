package twp.database.maps;

import org.bson.Document;
import org.bson.types.Binary;
import twp.database.core.Raw;

import java.io.File;
import java.nio.file.Paths;

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
}
