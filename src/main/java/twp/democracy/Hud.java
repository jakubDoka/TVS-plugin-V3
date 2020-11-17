package twp.democracy;

import arc.Events;
import mindustry.gen.Call;
import twp.Main;
import twp.database.PD;
import twp.database.Setting;
import twp.tools.Testing;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import static twp.Main.db;

public class Hud {

    ArrayList<Displayable> displayable = new ArrayList<>();

    public Hud() {
        displayable.add(Voting.processor);
        Events.on(Main.TickEvent.class, e -> update());
    }

    public void sendMessage(String message, Object[] args, int seconds, String ...colors) {
        Message.messages.add(new Message(message, args, seconds, colors));
    }

    void update() {
        db.online.forEachValue(iter -> {
            PD pd = iter.next();
            if(pd.player.p == null) {
                Testing.Log("pd.player.p is null when displaying hud");
                return;
            }
            if(!db.hasEnabled(pd.id, Setting.hud)) {
                Call.hideHudText(pd.player.p.con);
                return;
            }
            StringBuilder sb = new StringBuilder();

            for(Displayable displayable : displayable) {
                sb.append(displayable.getMessage(pd));
            }

            Iterator<Message> it = Message.messages.iterator();
            while (it.hasNext()){
                Message message = it.next();
                sb.append(message.getMessage(pd)).append("\n");
                if (message.counter < 1) {
                    it.remove();
                }
            }

            if(sb.length() == 0) {
                Call.hideHudText(pd.player.p.con);
            } else {
                Call.setHudText(pd.player.p.con, "[#cbcbcb]" + sb.substring(0, sb.length() - 1));
            }

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
            return String.format("[%s]%s[](%ds)", colors[counter % colors.length], pd.translate(message, args), counter);
        }
    }
}
