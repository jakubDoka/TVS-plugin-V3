package main.java.twp.democracy;

import mindustry.gen.Call;
import main.java.twp.Main;
import main.java.twp.database.PD;
import main.java.twp.database.enums.Setting;
import main.java.twp.tools.*;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.Map.Entry;

import static main.java.twp.Main.*;

// Hud manages updating of ingame hud, it also removes disconnected players from online list
public class Hud {

    ArrayList<Displayable> displayables = new ArrayList<>();

    public Hud() {
        displayables.add(Voting.processor);
        displayables.add(docks);
        Logging.on(Main.TickEvent.class, e -> update());
    }

    public void sendMessage(String message, Object[] args, int seconds, String ...colors) {
        Message.messages.add(new Message(message, args, seconds, colors));
    }

    public void update() {

        for (Iterator<Message> iter = Message.messages.iterator(); iter.hasNext();) {
            Message message = iter.next();
            message.tick();

            if (message.counter < 1) {
                iter.remove();
            }
        }
        
        for(Displayable displayable : displayables) {
            displayable.tick();
        }

        for(Iterator<Entry<String, PD>> iter = db.online.entrySet().iterator(); iter.hasNext(); ){
            PD pd = iter.next().getValue();
            if(pd.isInvalid()) {
                iter.remove();
                continue;
            }

            if(pd.disconnected()) {
                db.handler.free(pd);
                iter.remove();
                continue;
            }

            if(!db.hasEnabled(pd.id, Setting.hud)) {
                Call.hideHudText(pd.player.p.con);
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for(Displayable displayable : displayables) {
                sb.append(displayable.getMessage(pd));
            }

            for(Message message : Message.messages) {
                sb.append(message.getMessage(pd)).append("\n");
            }
            if(sb.length() == 0) {
                Call.hideHudText(pd.player.p.con);
            } else {
                Call.setHudText(pd.player.p.con, "[#cbcbcb]" + sb.substring(0, sb.length() - 1));
            }
        }
    }

    public interface Displayable {
        String getMessage(PD pd);
        void tick();
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

            return String.format("[%s]%s[](%ds)", colors[counter % colors.length], pd.translate(message, args), counter);
        }

        @Override
        public void tick() {
            counter--;
        }
    }
}
