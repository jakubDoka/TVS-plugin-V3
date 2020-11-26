package twp.commands;

import twp.database.DBPlayer;
import twp.database.enums.RankType;
import twp.database.enums.Stat;

import static twp.Main.db;
import static twp.Main.ranks;

class SetterTest extends Test {
    public static void main(String[] args) {
        init();

        Setter.terminal.freeAccess = true;

        db.handler.loadData(new DBPlayer(){{
            uuid = "a";
            ip = "a";
            name = "ca";
        }});

        db.handler.incOne(0, Stat.age);
        db.handler.incOne(0, Stat.buildCoreVotes);

        Setter.terminal.run("", "sda", "", "");
        Setter.terminal.assertResult(Command.Result.notInteger);

        Setter.terminal.run("", "1", "", "");
        Setter.terminal.assertResult(Command.Result.notFound);

        Setter.terminal.run("", "0", "asdasd", "");
        Setter.terminal.assertResult(Command.Result.wrongOption);

        Setter.terminal.run("", "0", "settings", "");
        Setter.terminal.assertResult(Command.Result.wrongAccess);

        Setter.terminal.run("", "0", "age", "asda");
        Setter.terminal.assertResult(Command.Result.notInteger);

        Setter.terminal.run("", "0", "age", "100");
        Setter.terminal.assertResult(Command.Result.success);

        Setter.terminal.run("", "0", "name", "ono");
        Setter.terminal.assertResult(Command.Result.success);

        Setter.terminal.run("", "0", "buildCoreVotes", "null");
        Setter.terminal.assertResult(Command.Result.unsetSuccess);

        db.online.put("", db.handler.loadData(new DBPlayer()));

        Setter.game.run("", "0", "buildCoreVotes", "null");
        Setter.game.assertResult(Command.Result.noPerm);

        db.handler.setRank(1, ranks.admin, RankType.rank);
        db.online.put("", db.handler.loadData(new DBPlayer()));

        Setter.game.run("", "0", "name", "null");
        Setter.game.assertResult(Command.Result.wrongAccess);
    }
}