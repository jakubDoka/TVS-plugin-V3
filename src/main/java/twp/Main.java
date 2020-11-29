package twp;


import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import twp.bundle.Bundle;
import twp.commands.*;
import twp.database.*;
import twp.database.maps.MapHandler;
import twp.democracy.Hud;
import twp.security.Limiter;
import mindustry.game.EventType;
import mindustry.mod.Plugin;
import twp.tools.MainQueue;

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

        Events.run(EventType.Trigger.update, ()-> {
            queue.run();
        });

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        RankSetter.terminal.registerCmp(handler, null);

        Searcher.terminal.registerCmp(handler, null);

        DBSetter.terminal.registerCmp(handler, null);

        MapManager.terminal.registerCmp(handler, null);

        handler.removeCommand("reloadmaps");
        handler.register("reloadmaps", "Reload all maps from disk.", arg -> {
            int beforeMaps = Vars.maps.all().size;
            Vars.maps.reload();
            if(!db.maps.validateMaps()) {
                Log.info("Some of maps are not valid, server will stop hosting when current game ends if you dont fix this issue.");
                db.maps.invalid = true;
                return;
            }
            db.maps.invalid = false;
            if(Vars.maps.all().size > beforeMaps){
                Log.info("@ new map(s) found and reloaded.", Vars.maps.all().size - beforeMaps);
            }else{
                Log.info("Maps reloaded.");
            }
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.removeCommand("vote");
        handler.removeCommand("votekick");

        RankSetter.game.registerGm(handler, null);

        Searcher.game.registerGm(handler, null);

        DBSetter.game.registerGm(handler, null);

        Voter.game.registerGm(handler, null);

        Mkgf.game.registerGm(handler, null);

        Mkgf.votekick.registerGm(handler, null);

        MapManager.game.registerGm(handler, null);

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
