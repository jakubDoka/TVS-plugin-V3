package twp.database.maps;

import arc.files.Fi;
import mindustry.io.MapIO;
import mindustry.maps.Map;
import twp.commands.tests.Test;

import java.io.IOException;

import static twp.Main.db;

public class MkgfTest extends Test {
    public static void main(String[] args) throws IOException {
        init();
        db.maps.drop();
        Map map = MapIO.createMap(new Fi("C:\\Users\\jakub\\Documents\\programming\\java\\mindustry_plugins\\TheWorstV3\\libs\\Novastar_V2.1.msav"), false);
        db.maps.makeNewMapData(map);
        db.maps.withdrawMap(0, "C:\\Users\\jakub\\Documents\\programming\\java\\mindustry_plugins\\TheWorstV3");
    }
}
