package twp.commands.tests;

import arc.util.*;
import twp.database.DBPlayer;
import twp.database.PD;
import twp.database.enums.RankType;
import twp.*;
import twp.commands.AccountManager;
import twp.commands.Command;

import static twp.Main.db;
import static twp.Main.ranks;

class AccountManagerTest extends Test {
    public static void main(String[] args) {
        init();

        db.online.put("", db.handler.loadData(new DBPlayer(){}));

        AccountManager.game.run( "", "new");
        AccountManager.game.assertResult(Command.Result.notExplicit);

        AccountManager.game.run( "", "protect");
        AccountManager.game.assertResult(Command.Result.notEnoughArgs);


        AccountManager.game.run( "","protect", "a");
        AccountManager.game.assertResult(Command.Result.confirm);

        AccountManager.game.run( "","protect", "b");
        AccountManager.game.assertResult(Command.Result.confirmFail);

        AccountManager.game.run( "","protect", "a");
        AccountManager.game.assertResult(Command.Result.confirm);

        AccountManager.game.run( "","protect", "a");
        AccountManager.game.assertResult(Command.Result.confirmSuccess);

        AccountManager.game.run("", "protect", "a");
        AccountManager.game.assertResult(Command.Result.alreadyProtected);

        AccountManager.game.run("", "unprotect", "b");
        AccountManager.game.assertResult(Command.Result.incorrectPassword);

        AccountManager.game.run("", "unprotect", "a");
        AccountManager.game.assertResult(Command.Result.unprotectSuccess);

        AccountManager.game.run("", "protect", "a");
        AccountManager.game.assertResult(Command.Result.confirm);

        AccountManager.game.run("", "protect", "a");
        AccountManager.game.assertResult(Command.Result.confirmSuccess);

        AccountManager.game.run("", "abandon");
        AccountManager.game.assertResult(Command.Result.success);

        AccountManager.game.run("", "e", "a");
        AccountManager.game.assertResult(Command.Result.notInteger);

        AccountManager.game.run("", "100", "a");
        AccountManager.game.assertResult(Command.Result.notFound);

        AccountManager.game.run("", "0", "b");
        AccountManager.game.assertResult(Command.Result.incorrectPassword);

        AccountManager.game.run("", "0", "a");
        AccountManager.game.assertResult(Command.Result.loginSuccess);

        db.handler.setRank(0, ranks.griefer, RankType.rank);
        PD data = db.handler.loadData(new DBPlayer(){});
        db.online.put("", data);

        AccountManager.game.run("");
        AccountManager.game.assertResult(Command.Result.noPerm);
    }
}