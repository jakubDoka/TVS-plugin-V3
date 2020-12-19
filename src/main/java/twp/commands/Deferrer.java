package twp.commands;

import mindustry.game.*;
import mindustry.gen.*;
import twp.database.*;
import twp.tools.*;

import static twp.Main.*;

public class Controller extends Command {
    boolean initialized;
    Runnable deferredCall;
    String reason;

    public Controller() {
        name = "deffer";
        argStruct = "<stop/close/recover> [reason]";
        description = "you can deffer call of some commands, it will be called on game over";
        if (!initialized) {
            initialized = true;
            Logging.on(EventType.GameOverEvent.class, e -> {
                if (deferredCall != null) {
                    for(Player p : Groups.player) {
                        PD pd = db.online.get(p.uuid());
                        if (pd != null) {
                            pd.kick("kick-custom", 0, reason);
                        }
                    }
                    deferredCall.run();
                }
            });
        }
    }

    @Override
    public void run(String id, String... args){
        switch (args[0]) {
            case "recover":
                deferredCall = null;
                return;
            case "stop":
            case "exit":
                deferredCall = () -> {
                    serverHandler.handleMessage(args[0]);
                };
                break;
            default:
                result = Result.wrongOption;
        }
    }

    public static Controller terminal = new Controller();
}
