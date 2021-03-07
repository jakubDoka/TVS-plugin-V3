package main.java.twp.commands.tests;

import mindustry.content.Items;
import main.java.twp.database.DBPlayer;
import main.java.twp.database.PD;

import static org.junit.Assert.assertEquals;
import static main.java.twp.Main.*;
import static main.java.twp.commands.LoadoutManager.*;
import static main.java.twp.commands.Command.Result.*;

public class LayoutManagerTest extends Test {
    public static void main(String[] args) {
        init();

        db.online.put("", db.handler.loadData(new DBPlayer()));
        game.caller = db.handler.loadData(new DBPlayer());
        game.run("", "get", "itm", "10");
        game.assertResult(invalidRequest);

        game.run("", "get", "copper", "h");
        game.assertResult(notInteger);

        docks.ships.size = 3;
        game.run("", "get", "copper", "10");
        game.assertResult(penalty);

        docks.ships.size = 0;
        game.run("", "", "copper", "10");
        game.assertResult(wrongOption);

        game.run("", "get", "spore-pod", "10");
        game.assertResult(redundant);

        db.loadout.set(Items.coal, 10000);
        game.run("", "get", "coal", "10000");
        game.assertResult(voteStartSuccess);

        assertEquals(3, docks.ships.size);
    }
}
