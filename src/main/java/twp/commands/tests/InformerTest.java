package main.java.twp.commands.tests;

import main.java.twp.commands.Command;
import main.java.twp.commands.Informer;
import main.java.twp.database.DBPlayer;

import static main.java.twp.Main.db;

public class InformerTest extends Test {
    public static void main(String[] args) {
        init();
        db.handler.loadData(new DBPlayer());
        Informer.general.run("", "0");
        Informer.general.assertResult(Command.Result.info);

        Informer.general.run("", "0", "stats");
        Informer.general.assertResult(Command.Result.stats);

        Informer.general.run("", "20", "stats");
        Informer.general.assertResult(Command.Result.notFound);
    }
}
