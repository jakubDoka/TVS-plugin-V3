package main.java.twp.commands.tests;

import main.java.twp.commands.*;
import main.java.twp.commands.Command.*;
import main.java.twp.database.*;
import main.java.twp.tools.*;

import static main.java.twp.Main.db;

public class TesterTest extends Test {
    public static void main(String[] args){
        init();

        db.online.put("", db.handler.loadData(new DBPlayer(){}));
        Tester.game.caller = db.online.get("");

        Tester.game.run("");
        Tester.game.assertResult(Result.hint);

        Tester.game.run("");
        Tester.game.assertResult(Result.hint);

        Tester.game.run("", "10");
        Tester.game.assertResult(Result.invalidRequest);

        Tester.game.run("", "-1");
        Tester.game.assertResult(Result.notInteger);

        Tester.game.run("", "1");
        Tester.game.assertResult(Result.hint);

        Tester.game.run("", "1");
        Tester.game.assertResult(Result.testFail);

        Tester.game.run("");
        Tester.game.assertResult(Result.penalty);

        Tester.game.recent.clear();

        Tester.game.run("","4");
        Tester.game.assertResult(Result.wrongOption);

        Tester.game.run("");
        Tester.game.assertResult(Result.hint);

        Tester.game.run("", "3");
        Tester.game.assertResult(Result.hint);

        Tester.game.run("", "3");
        Tester.game.assertResult(Result.success);
    }
}
