package twp;


import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Timer;
import twp.bundle.Bundle;
import twp.democracy.Hud;
import twp.discord.Bot;
import twp.security.Limiter;
import mindustry.Vars;
import mindustry.gen.Call;
import twp.commands.*;
import twp.database.*;
import mindustry.game.EventType;
import mindustry.mod.Plugin;
import twp.tools.*;

import static arc.Core.app;
import static arc.util.Log.info;
import static mindustry.Vars.net;

public class Main extends Plugin {
    public static Ranks ranks;
    public static Database db;
    public static Limiter lim;
    public static Hud hud;
    public static Bundle bundle;
    public static boolean testMode;
    public static CommandHandler serverHandler;
    public static Bot bot;

    public Main() {
        Global.loadConfig();

        Logging.on(EventType.ServerLoadEvent.class, e -> {
            ranks = new Ranks();
            db = new Database();
            lim = new Limiter();
            bundle = new Bundle();
            bot = new Bot();

            // this has to be last init
            hud = new Hud();
            if(!testMode) {
                Timer.schedule(() -> app.post(() -> Events.fire(new TickEvent())), 0, 1);
            }
        });

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        RankSetter.terminal.registerCmp(handler, null);

        Searcher.terminal.registerCmp(handler, null);

        DBSetter.terminal.registerCmp(handler, null);

        MapManager.terminal.registerCmp(handler, null);

        MapChanger.terminal.registerCmp(handler, null);

        Informer.general.registerCmp(handler, null);

        handler.removeCommand("exit");
        handler.register("exit", "Exit the server application.", arg -> {
            info("Shutting down server.");
            if(bot.api != null) {
                bot.api.disconnect();
            }
            net.dispose();
            Core.app.exit();
        });

        handler.removeCommand("reloadmaps");
        handler.register("reloadmaps", "Reload all maps from disk.", arg -> {
            int beforeMaps = Vars.maps.all().size;
            Vars.maps.reload();
            if(db.maps.invalidMaps()) {
                Logging.info("maps-notValid");
                Deferrer.terminal.run("", "close");
                return;
            }
            if(Vars.maps.all().size > beforeMaps){
                Logging.info("maps-reloadedCount", Vars.maps.all().size - beforeMaps);
            }else{
                Logging.info("maps-reloaded");
            }
        });

        handler.register("reload", "<bot/config>", "reloads stuff", (args) -> {
            switch (args[0]) {
                case "bot":
                    if (bot != null && bot.api != null) {
                        bot.api.disconnect();
                    }
                    bot = new Bot();
                    break;
                case "config":
                    Global.loadConfig();
                default:
                    Log.info("wrong option");
                    return;
            }
            Log.info("reloaded");
        });

        Deferrer.terminal.registerCmp(handler, null);

        serverHandler = handler;
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.removeCommand("vote");
        handler.removeCommand("votekick");

        Tester.game.registerGm(handler, null);

        VoteKick.game.registerGm(handler, null);

        RankSetter.game.registerGm(handler, null);

        MapChanger.game.registerGm(handler, null);

        Searcher.game.registerGm(handler, null);

        DBSetter.game.registerGm(handler, null);

        Voter.game.registerGm(handler, null);

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

        Informer.general.registerGm(handler, (self, pd) -> {
            pd.sendInfoMessage(self.getMessage(), self.arg);
        });

        handler.register("a", "test", (args, player)-> {
            Call.infoPopup("hello", 10, 100, 100, 100, 100, 100);
            Call.infoPopup("hello2", 10, 200, 200, 200, 200, 200);
        });
    }

    public static class TickEvent {}
}
