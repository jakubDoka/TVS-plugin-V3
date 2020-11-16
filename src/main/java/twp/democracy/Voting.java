package twp.democracy;

import arc.math.Mathf;
import twp.database.PD;
import twp.database.Perm;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static twp.Main.db;

public abstract class Voting {
    public static Processor processor = new Processor();
    int maxVotes = 5;
    public String parentName;

    int getMajority() {
        AtomicInteger counter = new AtomicInteger();
        db.online.forEachValue(pd -> {
            if(pd.canParticipate()) {
                counter.getAndIncrement();
            }
        });

        int count = counter.get();

        if(count == 1) {
            return 1;
        }

        return Mathf.clamp((count /2 + (count % 2 == 0 ? 0 : 1)), 2, maxVotes);
    }

    public Perm protection;

    public enum Messages {
        request,
        fail,
        success,
    }

    public abstract void run(Session session);

    public void revolve(Session session) {
        if(session.spacial && session.yes > session.no) {
            run(session);
            // TODO send success message
        } else {
            // TODO send fail message
        }
    }

    String getMessage(Messages messages) {
        return parentName + "-vote-" + messages.name();
    }

    public static class Session {
        public static final int duration = 60;

        Voting voting;
        int counter, yes, no;
        boolean spacial;
        Object[] args;

        public Session(boolean special, Voting voting, Object[] args) {
            counter = duration;
            if(special) {
                counter /= 3;
            }

            this.args = args;
            this.spacial = special;
            this.voting = voting;
        }

        void run() {
            voting.revolve(this);
        }

    }

    static class Processor implements Hud.Displayable {
        ArrayList<Session> sessions = new ArrayList<>();

        @Override
        public String getMessage(PD pd) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for(Session s : new ArrayList<>(sessions)) {
                int major = s.voting.getMajority();
                s.counter--;
                if(s.counter < 0) {
                    s.run();
                    continue;
                }

                sb.append(pd.translate(s.voting.getMessage(Messages.request), s.args));
                sb.append("\n");
                sb.append(pd.translate(s.spacial ? "voting-specialStatus" : "voting-status", i,
                        s.yes, s.no, major, s.counter));
                i++;
            }
            return sb.toString();
        }
    }
}
