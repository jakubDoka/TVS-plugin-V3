package twp;


import arc.Events;
import arc.func.Cons;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.gen.Call;
import twp.bundle.Bundle;
import twp.commands.AccountManager;
import twp.commands.Command;
import twp.commands.RankSetter;
import twp.commands.Searcher;
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
    public static boolean testMode;

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
        RankSetter.terminal.registerCmp(handler, null);

        Searcher.terminal.registerCmp(handler, null);
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        RankSetter.game.registerGm(handler, null);

        Searcher.game.registerCmp(handler, null);

        AccountManager.game.registerGm(handler, (self, pd) -> {
            switch (self.result){
                case loginSuccess:
                case success:
                    self.kickCaller(0);
                default:
                    self.notifyCaller();
            }
        });

        handler.register("a", "test", (args, player)-> {
            Call.infoPopup("hello", 10, 100, 100, 100, 100, 100);
            Call.infoPopup("hello2", 10, 200, 200, 200, 200, 200);
        });
    }
}
