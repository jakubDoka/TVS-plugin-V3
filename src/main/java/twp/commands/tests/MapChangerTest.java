package main.java.twp.commands.tests;

import main.java.twp.database.DBPlayer;

import static main.java.twp.Main.db;
import static main.java.twp.commands.MapChanger.*;
import static main.java.twp.commands.Command.Result.*;

public class MapChangerTest extends Test {
    // TODO test rate and change in game
    public static void main(String[] args) {
        init();
        db.online.put("", db.handler.loadData(new DBPlayer()));

        terminal.run("", "list");
        terminal.assertResult(none);

        terminal.run("", "list", "4");
        terminal.assertResult(none);

        terminal.run("", "list", "hh");
        terminal.assertResult(notInteger);

        terminal.run("", "flit");
        terminal.assertResult(wrongOption);
    }
}
