package twp.commands;

import twp.database.DBPlayer;

import static org.junit.jupiter.api.Assertions.*;
import static twp.Main.db;

class MkgfTest extends Test {
    public static void main(String[] args) {
        init();
        db.online.put("", db.handler.loadData(new DBPlayer()));
        db.handler.loadData(new DBPlayer(){{uuid = "asd";}});
        db.handler.loadData(new DBPlayer(){{uuid = "a";}});
        db.online.put("b", db.handler.loadData(new DBPlayer(){{uuid = "b";}}));

        Mkgf.game.run("", "mark", "100");
        Mkgf.game.assertResult(Command.Result.playerNotFound);

        Mkgf.game.run("", "unmark", "1");
        Mkgf.game.assertResult(Command.Result.redundant);

        Mkgf.game.run("", "mark", "1");
        Mkgf.game.assertResult(Command.Result.voteStartSuccess);

        Mkgf.game.run("", "mark", "1");
        Mkgf.game.assertResult(Command.Result.alreadyVoted);

        Mkgf.game.run("", "mark", "0");
        Mkgf.game.assertResult(Command.Result.cannotApplyToSelf);

        Mkgf.game.run("", "mark", "2");
        Mkgf.game.assertResult(Command.Result.alreadyVoting);

        Mkgf.game.run("b", "mark", "1");
        Mkgf.game.assertResult(Command.Result.voteSuccess);

        assertEquals("griefer", db.handler.get(1, "rank"));

        Mkgf.game.run("", "unmark", "1");
        Mkgf.game.assertResult(Command.Result.voteStartSuccess);

        Voter.game.run("b", "y");
        Voter.game.assertResult(Command.Result.voteSuccess);

        assertEquals("newcomer", db.handler.get(1, "rank"));
    }
}