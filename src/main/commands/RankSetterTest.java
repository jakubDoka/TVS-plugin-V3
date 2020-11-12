package main.commands;

import arc.util.Log;
import main.database.DBPlayer;
import main.database.PD;
import main.database.Raw;
import mindustry.gen.Player;

import static main.Main.db;
import static org.junit.jupiter.api.Assertions.*;

class RankSetterTest {
    public static void main(String[] args ) {
        db.handler.drop();
        db.handler.loadData(new DBPlayer(){});
        db.handler.loadData(new DBPlayer(){{uuid = "sdad"; ip = "asdasd";}});
        assertEquals(
                RankSetter.terminal.notFound,
                RankSetter.terminal.run(new String[]{"asdk", "admin"}, "")
        );
        assertEquals(
                RankSetter.terminal.success,
                RankSetter.terminal.run(new String[]{"0", "admin"}, "")
        );

        assertEquals(
                RankSetter.terminal.wrongRank,
                RankSetter.terminal.run(new String[]{"0", "asdasd"}, "")
        );
        assertEquals(
                RankSetter.game.noPerm,
                RankSetter.game.run(new String[]{"0", "newcomer"}, "")
        );

        PD data = db.handler.loadData(new DBPlayer(){});
        db.online.put(data.player.uuid, data);

        assertEquals(
                RankSetter.game.wrongAccess,
                RankSetter.game.run(new String[]{"0", "newcomer"}, "")
        );
        assertEquals(
                RankSetter.game.success,
                RankSetter.game.run(new String[]{"1", "verified"}, "")
        );
    }
}