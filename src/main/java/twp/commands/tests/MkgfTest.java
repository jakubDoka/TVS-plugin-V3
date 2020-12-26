package twp.commands.tests;

import twp.database.DBPlayer;
import twp.commands.Command;
import twp.commands.VoteKick;
import twp.commands.Voter;

import static org.junit.Assert.assertEquals;
import static twp.Main.db;

class MkgfTest extends Test {
    public static void main(String[] args) {
        init();
        db.online.put("", db.handler.loadData(new DBPlayer()));
        db.handler.loadData(new DBPlayer(){{uuid = "asd";}});
        db.handler.loadData(new DBPlayer(){{uuid = "a";}});
        db.online.put("b", db.handler.loadData(new DBPlayer(){{uuid = "b";}}));

        VoteKick.game.run("", "1", "asdas");
        VoteKick.game.assertResult(Command.Result.wrongOption);

        VoteKick.game.run("", "100");
        VoteKick.game.assertResult(Command.Result.playerNotFound);

        VoteKick.game.run("",  "1", "unmark");
        VoteKick.game.assertResult(Command.Result.redundant);

        VoteKick.game.run("",  "1");
        VoteKick.game.assertResult(Command.Result.voteStartSuccess);

        VoteKick.game.run("",  "1");
        VoteKick.game.assertResult(Command.Result.alreadyVoted);

        VoteKick.game.run("",  "0");
        VoteKick.game.assertResult(Command.Result.cannotApplyToSelf);

        VoteKick.game.run("",  "2");
        VoteKick.game.assertResult(Command.Result.alreadyVoting);

        VoteKick.game.run("b",  "1");
        VoteKick.game.assertResult(Command.Result.voteSuccess);

        assertEquals("griefer", db.handler.get(1, "rank"));

        VoteKick.game.run("",  "1", "unmark");
        VoteKick.game.assertResult(Command.Result.voteStartSuccess);

        Voter.game.run("b", "y");
        Voter.game.assertResult(Command.Result.voteSuccess);

        assertEquals("newcomer", db.handler.get(1, "rank"));
    }
}