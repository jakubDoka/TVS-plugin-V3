package twp.commands;

import arc.Events;
import twp.Main;
import twp.database.DBPlayer;
import twp.database.PD;
import twp.database.RankType;
import mindustry.game.EventType;

import static twp.Main.db;
import static twp.Main.ranks;

class AccountManagerTest {
    public static void main(String[] args) {
        new Main();
        Events.fire(new EventType.ServerLoadEvent());
        db.handler.drop();
        db.online.put("", db.handler.loadData(new DBPlayer(){}));

        AccountManager.game.run(new String[]{"new"}, "");
        AccountManager.game.assertResult(Command.Result.notExplicit);

        AccountManager.game.run(new String[]{"protect"}, "");
        AccountManager.game.assertResult(Command.Result.notEnoughArgs);


        AccountManager.game.run(new String[]{"protect", "a"}, "");
        AccountManager.game.assertResult(Command.Result.confirm);

        AccountManager.game.run(new String[]{"protect", "b"}, "");
        AccountManager.game.assertResult(Command.Result.confirmFail);

        AccountManager.game.run(new String[]{"protect", "a"}, "");
        AccountManager.game.assertResult(Command.Result.confirm);

        AccountManager.game.run(new String[]{"protect", "a"}, "");
        AccountManager.game.assertResult(Command.Result.confirmSuccess);

        AccountManager.game.run(new String[]{"protect", "a"}, "");
        AccountManager.game.assertResult(Command.Result.alreadyProtected);

        AccountManager.game.run(new String[]{"unprotect", "b"}, "");
        AccountManager.game.assertResult(Command.Result.incorrectPassword);

        AccountManager.game.run(new String[]{"unprotect", "a"}, "");
        AccountManager.game.assertResult(Command.Result.unprotectSuccess);

        AccountManager.game.run(new String[]{"protect", "a"}, "");
        AccountManager.game.assertResult(Command.Result.confirm);

        AccountManager.game.run(new String[]{"protect", "a"}, "");
        AccountManager.game.assertResult(Command.Result.confirmSuccess);

        AccountManager.game.run(new String[]{"abandon"}, "");
        AccountManager.game.assertResult(Command.Result.success);

        AccountManager.game.run(new String[]{"e", "a"}, "");
        AccountManager.game.assertResult(Command.Result.notInteger);

        AccountManager.game.run(new String[]{"100", "a"}, "");
        AccountManager.game.assertResult(Command.Result.notFound);

        AccountManager.game.run(new String[]{"0", "b"}, "");
        AccountManager.game.assertResult(Command.Result.incorrectPassword);

        AccountManager.game.run(new String[]{"0", "a"}, "");
        AccountManager.game.assertResult(Command.Result.loginSuccess);

        db.handler.setRank(0, ranks.griefer, RankType.rank);
        PD data = db.handler.loadData(new DBPlayer(){});
        db.online.put("", data);

        AccountManager.game.run(new String[]{"", ""}, "");
        AccountManager.game.assertResult(Command.Result.noPerm);
    }
}