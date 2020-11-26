package twp.commands.tests;

import twp.commands.Command;
import twp.commands.MapManager;
import twp.database.maps.MapHandler;

import static org.junit.jupiter.api.Assertions.*;
import static twp.Main.db;

class MapManagerTest extends Test {
    public static void main(String[] args) {
        init();
        MapHandler.mapFolder = "C:\\Users\\jakub\\Documents\\programming\\java\\mindustry_plugins\\TheWorstV3";

        MapManager.terminal.run("", "add", "nonexistent.msav");
        MapManager.terminal.assertResult(Command.Result.notExist);

        MapManager.terminal.run("", "add", "C:\\Users\\jakub\\Documents\\programming\\java\\mindustry_plugins\\TheWorstV3\\libs\\Novastar_V2.1.msav");
        MapManager.terminal.assertResult(Command.Result.addSuccess);

        MapManager.terminal.run("", "add", "C:\\Users\\jakub\\Documents\\programming\\java\\mindustry_plugins\\TheWorstV3\\libs\\Novastar_V2.1.msav");
        MapManager.terminal.assertResult(Command.Result.alreadyAdded);

        MapManager.terminal.run("", "add", "C:\\Users\\jakub\\Documents\\programming\\java\\mindustry_plugins\\TheWorstV3\\libs\\dummy.msav");
        MapManager.terminal.assertResult(Command.Result.invalidFile);

        MapManager.terminal.run("", "update", "C:\\Users\\jakub\\Documents\\programming\\java\\mindustry_plugins\\TheWorstV3\\libs\\Novastar_V2.1.msav");
        MapManager.terminal.assertResult(Command.Result.updateSuccess);

        MapManager.terminal.run("", "enable", "noainteger");
        MapManager.terminal.assertResult(Command.Result.notInteger);

        MapManager.terminal.run("", "enable", "0");
        MapManager.terminal.assertResult(Command.Result.success);

        MapManager.terminal.run("", "enable", "0");
        MapManager.terminal.assertResult(Command.Result.alreadyEnabled);

        MapManager.terminal.run("", "disable", "0");
        MapManager.terminal.assertResult(Command.Result.success);

        MapManager.terminal.run("", "disable", "0");
        MapManager.terminal.assertResult(Command.Result.alreadyDisabled);

        MapManager.terminal.run("", "remove", "0");
        MapManager.terminal.assertResult(Command.Result.success);

        assertNull(db.maps.getMap(0));

        MapManager.terminal.run("", "remove", "0");
        MapManager.terminal.assertResult(Command.Result.notFound);
    }

}