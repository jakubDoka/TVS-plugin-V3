package twp.commands;

import jdk.incubator.jpackage.internal.*;
import mindustry.game.*;
import mindustry.gen.*;
import twp.database.*;
import twp.democracy.*;
import twp.tools.*;

import static twp.Main.*;

public class Deferrer extends Command {
    boolean initialized;
    Runnable deferredCall;
    String reason;

    public Deferrer() {
        name = "defer";
        argStruct = "<stop/exit/recover> [reason]";
        description = "you can deffer call of some commands, it will be called on game over";
        if (!initialized) {
            initialized = true;
            Logging.on(EventType.GameOverEvent.class, e -> {
                if (deferredCall != null) {
                    db.online.forEachValue(pd -> pd.next().kick("kick-custom", 0, reason));
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
                result = Result.recoverSuccess;
                return;
            case "stop":
            case "exit":
                deferredCall = () -> {
                    serverHandler.handleMessage(args[0]);
                };
                break;
            default:
                result = Result.wrongOption;
                return;
        }

        queue.post(() -> hud.sendMessage("deferrer-closing", new Object[0], 30, "grey", "red"));
    }

    public static Deferrer terminal = new Deferrer();
}
