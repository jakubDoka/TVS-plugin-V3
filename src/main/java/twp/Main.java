package twp;


import arc.Events;
import arc.func.Cons;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;
import mindustry.gen.Call;
import mindustry.world.blocks.logic.LogicBlock;
import twp.bundle.Bundle;
import twp.commands.*;
import twp.database.*;
import twp.democracy.Hud;
import twp.security.Limiter;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import twp.tools.MainQueue;

import static mindustry.Vars.world;

public class Main extends Plugin {
    public static Ranks ranks;
    public static Database db;
    public static Limiter lim;
    public static Hud hud;
    public static Bundle bundle;
    public static boolean testMode;

    public static MainQueue queue = new MainQueue();

    public Main() {


        Events.on(EventType.ServerLoadEvent.class, e -> {
            ranks = new Ranks();
            db = new Database();
            lim = new Limiter();
            bundle = new Bundle();

            // this has to be last init
            hud = new Hud();
            if(!testMode) {
                Timer.schedule(() -> queue.post(() -> Events.fire(new TickEvent())), 0, 1);
            }
        });

        Events.run(EventType.Trigger.update, ()-> queue.run());


    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        RankSetter.terminal.registerCmp(handler, null);

        Searcher.terminal.registerCmp(handler, null);

        Setter.terminal.registerCmp(handler, null);
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.removeCommand("vote");
        handler.removeCommand("votekick");

        RankSetter.game.registerGm(handler, null);

        Searcher.game.registerGm(handler, null);

        Setter.game.registerGm(handler, null);

        Voter.game.registerGm(handler, null);

        Mkgf.game.registerGm(handler, null);

        Mkgf.votekick.registerGm(handler, null);

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

    public static class TickEvent {}
}
