package twp.commands.tests;

import twp.database.DBPlayer;
import twp.database.enums.RankType;
import twp.database.enums.Stat;
import twp.commands.Command;
import twp.commands.DBSetter;

import static twp.Main.db;
import static twp.Main.ranks;

class SetterTest extends Test {
    public static void main(String[] args) {
        init();

        DBSetter.terminal.freeAccess = true;

        db.handler.loadData(new DBPlayer(){{
            uuid = "a";
            ip = "a";
            name = "ca";
        }});

        db.handler.incOne(0, Stat.age);
        db.handler.incOne(0, Stat.buildCoreVotes);

        DBSetter.terminal.run("", "sda", "", "");
        DBSetter.terminal.assertResult(Command.Result.notInteger);

        DBSetter.terminal.run("", "1", "", "");
        DBSetter.terminal.assertResult(Command.Result.notFound);

        DBSetter.terminal.run("", "0", "asdasd", "");
        DBSetter.terminal.assertResult(Command.Result.wrongOption);

        DBSetter.terminal.run("", "0", "settings", "");
        DBSetter.terminal.assertResult(Command.Result.wrongAccess);

        DBSetter.terminal.run("", "0", "age", "asda");
        DBSetter.terminal.assertResult(Command.Result.notInteger);

        DBSetter.terminal.run("", "0", "age", "100");
        DBSetter.terminal.assertResult(Command.Result.success);

        DBSetter.terminal.run("", "0", "name", "ono");
        DBSetter.terminal.assertResult(Command.Result.success);

        DBSetter.terminal.run("", "0", "buildCoreVotes", "null");
        DBSetter.terminal.assertResult(Command.Result.unsetSuccess);

        db.online.put("", db.handler.loadData(new DBPlayer()));

        DBSetter.game.run("", "0", "buildCoreVotes", "null");
        DBSetter.game.assertResult(Command.Result.noPerm);

        db.handler.setRank(1, ranks.admin, RankType.rank);
        db.online.put("", db.handler.loadData(new DBPlayer()));

        DBSetter.game.run("", "0", "name", "null");
        DBSetter.game.assertResult(Command.Result.wrongAccess);
    }
}