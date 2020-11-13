package twp;


import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.gen.Call;
import twp.bundle.Bundle;
import twp.commands.AccountManager;
import twp.commands.RankSetter;
import twp.database.*;
import twp.security.Limiter;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

public class Main extends Plugin {
    public static Ranks ranks;
    public static Database db;
    public static Limiter lim;
    public static Bundle bundle;

    public Main() {
        Events.on(EventType.ServerLoadEvent.class, e -> {
            ranks = new Ranks();
            db = new Database();
            lim = new Limiter();
            bundle = new Bundle();
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        RankSetter.terminal.register(handler, (args) -> {
            Log.info(RankSetter.terminal.run(args, ""));
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        RankSetter.game.register(handler, (result, message, pd) -> {
            pd.sendServerMessage(result);
        });

        AccountManager.game.register(handler, (result, message, pd) -> {
            if(result.equals(AccountManager.game.success) || result.equals(AccountManager.game.successLogin)) {
                pd.kick(message, 0);
            } else {
                pd.sendServerMessage(message);
            }
        });

        handler.register("a", "test", (args, player)-> {
            Call.infoToast("hello", 10);
        });
    }
}
