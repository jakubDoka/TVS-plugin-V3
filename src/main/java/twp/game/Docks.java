package main.java.twp.game;

import arc.struct.Seq;
import arc.util.Log;
import main.java.twp.database.PD;
import main.java.twp.democracy.Hud;

import static main.java.twp.Main.*;
import static main.java.twp.tools.Text.secToTime;

public class Docks implements Hud.Displayable {
    public Seq<Ship> ships = new Seq<>();

    public void use(Ship ship) {
        queue.post(() -> ships.add(ship));
        if(testMode) queue.run();
    }

    public boolean canUse() {
       return ships.size < config.shipLimit;
    }

    @Override
    public String getMessage(PD pd) {
        StringBuilder sb = new StringBuilder();
        ships.forEach(s -> sb.append(s.string()));

        if(sb.length() == 0) {
            return "";
        }

        sb.append("\n");

        return sb.toString();
    }

    @Override
    public void tick() {
        ships.filter(s -> {
            s.time--;
            if(s.time <= 0) {
                s.onDelivery.run();
                return true;
            }
            return false;
        });
    }

    public static class Ship {
        int time;
        String message;
        Runnable onDelivery;

        public static String
                itemsFromCore = "<--%s<--\uf851",
                itemsToCore = "-->%s-->\uf869";

        public Ship(String message, Runnable onDelivery, int time) {
            this.message = message;
            this.onDelivery = onDelivery;
            this.time = time;
        }

        public String string() {
            return String.format( "[gray]<>[]"+message+"[gray]<>[]", secToTime(time));
        }
    }
}
