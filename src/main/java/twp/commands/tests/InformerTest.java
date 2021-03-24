package twp.commands.tests;

import twp.commands.Command;
import twp.commands.Informer;
import twp.database.DBPlayer;

import static twp.Main.db;

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
