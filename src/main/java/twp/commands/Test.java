package twp.commands;

import arc.Events;
import mindustry.game.EventType;
import twp.Main;

import static twp.Main.db;

public class Test {
    public static void init() {
        new Main();
        Main.testMode = true;
        Events.fire(new EventType.ServerLoadEvent());
        db.handler.drop();
        db.maps.drop();
    }
}
