package twp.commands;

import twp.database.PD;
import twp.democracy.Voting;

import static twp.Main.db;

public class Voter extends Command {

    @Override
    void run(String id, String... args) {
        if(cannotInteract(id)) {
            return;
        }

        int idx = 0;

        if(args.length > 1) {
            if(isNotInteger(args, 1)) {
                return;
            }
            idx = Integer.parseInt(args[1]);
        }

        result = Voting.processor.addVote(idx, db.online.get(id).id, args[0]);
    }

    public static Voter game = new Voter();
}
