package twp.commands;

import arc.util.Log;
import twp.database.DBPlayer;

import static org.junit.jupiter.api.Assertions.*;
import static twp.Main.db;

class SearcherTest extends Test {
    public static void main(String[] args) {
        init();

        db.handler.loadData(new DBPlayer(){{
            uuid = "a";
            ip = "a";
            name = "ca";
        }});
        db.handler.loadData(new DBPlayer(){{
            uuid = "b";
            ip = "b";
            name = "cb";
        }});
        db.handler.loadData(new DBPlayer(){{
            uuid = "c";
            ip = "c";
            name = "c";
        }});
        db.handler.loadData(new DBPlayer(){{
            uuid = "d";
            ip = "d";
            name = "d";
        }});

        Searcher.terminal.run("", "c");
        Searcher.terminal.assertResult(Command.Result.success);
        Log.info(Searcher.terminal.arg[0]);

        Searcher.terminal.run("", "none");
        Searcher.terminal.assertResult(Command.Result.success);
        Log.info(Searcher.terminal.arg[0]);

        Searcher.terminal.run("", "none", "age");
        Searcher.terminal.assertResult(Command.Result.success);
        Log.info(Searcher.terminal.arg[0]);

        Searcher.terminal.run("", "none", "age", "2=4");
        Searcher.terminal.assertResult(Command.Result.success);
        Log.info(Searcher.terminal.arg[0]);

        Searcher.terminal.run("", "none", "age", "-1=-3");
        Searcher.terminal.assertResult(Command.Result.success);
        Log.info(Searcher.terminal.arg[0]);

        Searcher.terminal.run("", "none", "age", "-1=-3", "inv");
        Searcher.terminal.assertResult(Command.Result.success);
        Log.info(Searcher.terminal.arg[0]);

        Searcher.terminal.run("", "none", "age", "v");
        Searcher.terminal.assertResult(Command.Result.invalidSlice);
        Log.info(Searcher.terminal.arg[0]);

        Searcher.terminal.run("", "none", "age", "10-10");
        Searcher.terminal.assertResult(Command.Result.emptySlice);
        Log.info(Searcher.terminal.arg[0]);
    }
}