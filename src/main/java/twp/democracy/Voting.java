package main.java.twp.democracy;

import arc.math.Mathf;
import main.java.twp.commands.Command;
import main.java.twp.database.*;
import main.java.twp.database.enums.Perm;
import main.java.twp.database.enums.Stat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static main.java.twp.Main.*;

// Voting handles all of the vote sessions
public class Voting {
    public static Processor processor = new Processor();
    int maxVotes, minVotes;
    public Command parent;
    public String name;
    public Perm protection;
    public Stat increase;

    public Voting(Command parent, String name, int minVotes, int maxVotes) {
        this.name = name;
        this.parent = parent;
        this.maxVotes = maxVotes;
        this.minVotes = minVotes;
    }

    int getMajority() {
        AtomicInteger counter = new AtomicInteger();
        for(PD pd : db.online.values()) {
            if(pd.canParticipate()) {
                counter.getAndIncrement();
            }
        }

        int count = counter.get();

        if(count == 1) {
            return minVotes;
        }

        return Mathf.clamp((count /2 + (count % 2 == 0 ? 0 : 1)), 2, maxVotes);
    }

    public Command.Result pushSession(PD pd, VoteRunner runner, Object ...args) {
        if(processor.isVoting(pd.id)) {
            return Command.Result.alreadyVoting;
        }

        if(pd.cannotInteract()) {
            return Command.Result.cannotVote;
        }

        processor.addSession(new Session(pd.hasThisPerm(protection), this, runner, args, pd.id));
        processor.addVote(pd.id);

        return Command.Result.voteStartSuccess;
    }



    public enum Messages {
        request,
        fail,
        success,
    }

    public void revolve(Session session) {
        // session is always special at the end unless time runs out
        if(session.spacial && session.yes > session.no) {
            if(increase != null) {
                db.handler.inc(session.owner, increase, 1);
            }
            session.runner.run(session);
            if(testMode) return;
            hud.sendMessage(getMessage(Messages.success), session.args, 10, "green", "gray");
        } else {
            if(testMode) return;
            hud.sendMessage(getMessage(Messages.fail), session.args, 10, "red", "gray");
        }
    }

    String getMessage(Messages messages) {
        return parent.name + "-" + name + "-" + messages.name();
    }

    public interface VoteRunner {
        void run(Session session);
    }

    public static class Session {
        public static final int duration = 60;

        public Voting voting;
        VoteRunner runner;
        int counter, yes, no;
        boolean spacial;
        long owner;
        HashSet<Long> voted = new HashSet<>();
        public Object[] args;

        public Session(boolean special, Voting voting, VoteRunner runner, Object[] args, long owner) {
            counter = duration;
            if(special) {
                counter /= 3;
            }

            this.runner = runner;
            this.owner = owner;
            this.args = args;
            this.spacial = special;
            this.voting = voting;
        }

        void run() {
            voting.revolve(this);
        }

    }

    public static class Processor implements Hud.Displayable {
        ArrayList<Session> sessions = new ArrayList<>();

        public int query(Query con) {
            int i = 0;
            for(Session s : sessions) {
                if(con.get(s)) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        public boolean isVoting(long id) {
            for(Session s : sessions) {
                if(s.owner == id) {
                    return true;
                }
            }
            return false;
        }

        public void addSession(Session session) {
            sessions.add(session);
        }

        public Command.Result addVote(int idx, long id, String vote) {
            if(sessions.size() <= idx) {
                return Command.Result.invalidVoteSession;
            }

            Session s = sessions.get(idx);

            if(s.voted.contains(id)) {
                return Command.Result.alreadyVoted;
            }

            s.voted.add(id);

            if ("y".equals(vote)) {
                s.yes++;
            } else {
                s.no++;
            }

            int major = s.voting.getMajority();

            if(s.yes >= major || s.no >= major || testMode) {
                s.spacial = true;
                s.run();
                sessions.remove(s);
            }

            return Command.Result.voteSuccess;
        }

         void addVote(long id) {
            addVote(sessions.size() - 1, id, "y");
        }

        @Override
        public  String getMessage(PD pd) {
            StringBuilder sb = new StringBuilder();
            int i = 0;

            for(Session s : new ArrayList<>(sessions)) {
                int major = s.voting.getMajority();
                Account ac = db.handler.getAccount(s.owner);

                sb.append(s.counter % 2 == 0 ? "[gray]" : "[white]");
                sb.append(pd.translate(s.voting.getMessage(Messages.request), s.args));
                sb.append("[]\n");
                if (s.spacial) {
                    sb.append(pd.translate("vote-specialStatus", i, s.yes, s.no, ac.getName(), s.owner, s.counter));
                } else {
                    sb.append(pd.translate("vote-status", i, s.yes, s.no, major, ac.getName(), s.owner, s.counter));
                }

                sb.append("\n");

                i++;
            }
            return sb.toString();
        }

        @Override
        public void tick() {
            for(Session s : new ArrayList<>(sessions)) {
                s.counter--;
                if (s.counter < 0) {
                    s.run();
                    sessions.remove(s);
                }
            }
        }

        public interface Query {
            boolean get(Session s);
        }
    }
}
