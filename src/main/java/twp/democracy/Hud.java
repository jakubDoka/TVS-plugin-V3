package twp.democracy;

import arc.Events;
import mindustry.gen.Call;
import twp.Main;
import twp.database.PD;
import twp.database.Setting;
import twp.tools.Testing;

import java.awt.*;
import java.util.ArrayList;

import static twp.Main.db;

public class Hud {

    ArrayList<Displayable> displayable = new ArrayList<>();

    public Hud() {
        Events.on(Main.TickEvent.class, e -> update());
    }

    public void sendMessage(String message, Object[] args, int seconds, String ...colors) {
        Message.messages.add(new Message(message, args, seconds, colors));
    }

    void update() {
        db.online.forEachValue(pd -> {
            if(!db.hasEnabled(pd.id, Setting.hud)) {
                return;
            }
            StringBuilder sb = new StringBuilder();

            for(Displayable displayable : displayable) {
                sb.append(displayable.getMessage(pd)).append("\n");
            }

            for(Message message  : Message.messages) {
                sb.append(message.getMessage(pd)).append("\n");
            }

            if(pd.player.p == null) {
                Testing.Log("pd.player.p is null when displaying hud");
                return;
            }

            Call.setHudText(pd.player.p.con, sb.substring(0, sb.length() - 1));
        });

    }

    public interface Displayable {
        String getMessage(PD pd);
    }

    static class Message implements Displayable {
        static ArrayList<Message> messages = new ArrayList<>();

        int counter;
        Object[] args;
        String message;
        String[] colors;

        Message(String message, Object[] args, int counter, String ...colors) {
            this.message = message;
            this.counter = counter;
            this.args = args;

            if(colors.length == 0) {
                this.colors = new String[]{"white"};
            } else {
                this.colors = colors;
            }
        }


        @Override
        public String getMessage(PD pd) {
            counter--;
            if (counter < 1) {
                messages.remove(this);
            }
            return String.format("[%s]%s[](%ds)", colors[counter % colors.length], pd.translate(message, args), counter);
        }
    }
}
