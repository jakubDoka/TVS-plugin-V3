package twp.commands.tests;

import twp.database.DBPlayer;
import twp.database.PD;
import twp.commands.Command;
import twp.commands.RankSetter;

import static twp.Main.db;

class RankSetterTest extends Test {
    public static void main(String[] args ) {
        init();

        db.handler.loadData(new DBPlayer(){});
        db.handler.loadData(new DBPlayer(){{uuid = "sdad"; ip = "asdasd";}});

        RankSetter.terminal.run("", "asdk", "admin");
        RankSetter.terminal.assertResult(Command.Result.playerNotFound);

        RankSetter.terminal.run("", "0", "admin");
        RankSetter.terminal.assertResult(Command.Result.success);

        RankSetter.terminal.run("", "0", "asdasd");
        RankSetter.terminal.assertResult(Command.Result.wrongRank);

        RankSetter.game.run("", "0", "newcomer");
        RankSetter.game.assertResult(Command.Result.noPerm);

        PD data = db.handler.loadData(new DBPlayer(){});
        db.online.put(data.player.uuid, data);

        RankSetter.game.run("", "0", "newcomer");
        RankSetter.game.assertResult(Command.Result.wrongAccess);

        RankSetter.game.run("", "1", "verified");
        RankSetter.game.assertResult(Command.Result.success);
    }
}