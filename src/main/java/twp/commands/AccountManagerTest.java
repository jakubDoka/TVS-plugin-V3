package twp.commands;

import arc.Events;
import twp.Main;
import twp.database.DBPlayer;
import twp.database.PD;
import twp.database.RankType;
import mindustry.game.EventType;

import static twp.Main.db;
import static twp.Main.ranks;
import static org.junit.jupiter.api.Assertions.*;

class AccountManagerTest {
    public static void main(String[] args) {
        new Main();
        Events.fire(new EventType.ServerLoadEvent());
        db.handler.drop();
        db.online.put("", db.handler.loadData(new DBPlayer(){}));
        assertEquals(
                AccountManager.game.notExplicit,
                AccountManager.game.run(new String[]{"new"}, "")
        );

        assertEquals(
                AccountManager.game.notEnoughArgs,
                AccountManager.game.run(new String[]{"protect"}, "")
        );

        assertEquals(
                AccountManager.game.confirm,
                AccountManager.game.run(new String[]{"protect", "a"}, "")
        );

        assertEquals(
                AccountManager.game.confirmFail,
                AccountManager.game.run(new String[]{"protect", "b"}, "")
        );

        assertEquals(
                AccountManager.game.confirm,
                AccountManager.game.run(new String[]{"protect", "a"}, "")
        );

        assertEquals(
                AccountManager.game.confirmSuccess,
                AccountManager.game.run(new String[]{"protect", "a"}, "")
        );

        assertEquals(
                AccountManager.game.alreadyProtected,
                AccountManager.game.run(new String[]{"protect", "a"}, "")
        );

        assertEquals(
                AccountManager.game.incorrectPassword,
                AccountManager.game.run(new String[]{"unprotect", "b"}, "")
        );

        assertEquals(
                AccountManager.game.unprotectSuccess,
                AccountManager.game.run(new String[]{"unprotect", "a"}, "")
        );

        assertEquals(
                AccountManager.game.confirmSuccess,
                AccountManager.game.run(new String[]{"protect", "a"}, "")
        );

        assertEquals(
                AccountManager.game.alreadyProtected,
                AccountManager.game.run(new String[]{"protect", "a"}, "")
        );

        assertEquals(
                AccountManager.game.success,
                AccountManager.game.run(new String[]{"abandon"}, "")
        );

        assertEquals(
                AccountManager.game.notInteger,
                AccountManager.game.run(new String[]{"e", "a"}, "")
        );

        assertEquals(
                AccountManager.game.notFound,
                AccountManager.game.run(new String[]{"100", "a"}, "")
        );

        assertEquals(
                AccountManager.game.incorrectPassword,
                AccountManager.game.run(new String[]{"0", "b"}, "")
        );

        assertEquals(
                AccountManager.game.successLogin,
                AccountManager.game.run(new String[]{"0", "a"}, "")
        );

        db.handler.setRank(0, ranks.griefer, RankType.rank);
        PD data = db.handler.loadData(new DBPlayer(){});
        db.online.put("", data);

        assertEquals(
                AccountManager.game.noPerm,
                AccountManager.game.run(new String[]{"", ""}, "")
        );
    }
}