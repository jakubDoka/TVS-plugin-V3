package twp.democracy;

import arc.func.Cons;
import arc.math.Mathf;
import arc.util.Log;
import twp.commands.Command;
import twp.database.PD;
import twp.database.Perm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static twp.Main.db;
import static twp.Main.hud;

public class Voting {
    public static Processor processor = new Processor();
    int maxVotes, minVotes;
    public String parentName;
    public Perm protection;

    public Voting(String parentName, int maxVotes, int minVotes) {
        this.parentName = parentName;
        this.maxVotes = maxVotes;
        this.minVotes = minVotes;
    }

    int getMajority() {
        AtomicInteger counter = new AtomicInteger();
        db.online.forEachValue(pd -> {
            if(pd.canParticipate()) {
                counter.getAndIncrement();
            }
        });

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
        if(session.spacial && session.yes > session.no) {
            session.runner.run(session);
            hud.sendMessage(getMessage(Messages.success), session.args, 10, "green", "gray");
        } else {
            hud.sendMessage(getMessage(Messages.fail), session.args, 10, "red", "gray");
        }
    }

    String getMessage(Messages messages) {
        return parentName + "-vote-" + messages.name();
    }

    public interface VoteRunner {
        void run(Session session);
    }

    public static class Session {
        public static final int duration = 60;

        Voting voting;
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

        public synchronized int query(Query con) {
            int i = 0;
            for(Session s : sessions) {
                if(con.get(s)) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        public synchronized boolean isVoting(long id) {
            for(Session s : sessions) {
                if(s.owner == id) {
                    return true;
                }
            }
            return false;
        }

        public synchronized void addSession(Session session) {
            sessions.add(session);
        }

        public synchronized Command.Result addVote(int idx, long id, String vote) {
            if(sessions.size() <= idx) {
                return Command.Result.invalidVoteSession;
            }

            Session s = sessions.get(idx);

            if(s.voted.contains(id)) {
                return Command.Result.alreadyVoted;
            }

            switch (vote) {
                case "y":
                    s.yes++;
                    break;
                case "n":
                    s.no++;
            }

            int major = s.voting.getMajority();

            if(s.yes >= major || s.no >= major) {
                s.spacial = true;
                s.run();
                sessions.remove(s);
            }

            return Command.Result.voteSuccess;
        }

        synchronized void addVote(long id) {
            Session s = sessions.get(sessions.size() - 1);
            s.yes++;
            s.voted.add(id);
        }

        @Override
        public synchronized String getMessage(PD pd) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for(Session s : new ArrayList<>(sessions)) {
                int major = s.voting.getMajority();
                s.counter--;
                if(s.counter < 0) {
                    s.run();
                    sessions.remove(s);
                    continue;
                }

                sb.append(pd.translate(s.voting.getMessage(Messages.request), s.args));
                sb.append("\n");
                sb.append(pd.translate(s.spacial ? "voting-specialStatus" : "voting-status", i, s.yes, s.no, major));
                i++;
            }
            return sb.toString();
        }

        public interface Query {
            boolean get(Session s);
        }
    }
}
