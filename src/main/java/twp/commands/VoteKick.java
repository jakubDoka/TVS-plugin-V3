package twp.commands;

import twp.database.Account;
import twp.database.PD;
import twp.database.enums.Perm;
import twp.database.enums.Stat;
import twp.democracy.Voting;

import static twp.Main.db;

public class VoteKick extends Command {
    Voting main = new Voting(this, "main", 5, 2) {
        {
            protection = Perm.antiGrief;
            increase = Stat.mkgfVotes;
        }
    };

    public VoteKick() {
        name = "votekick";
        argStruct = "<id/name> [unmark]";
        description = "Marks player a griefer witch means he/she can only spectate.";
    }

    @Override
    public void run(String id, String... args) {
        PD pd = db.online.get(id);

        if(cannotInteract(id)) {
            return;
        }

        Account account = db.findAccount(args[0]);

        if(account == null) {
            playerNotFound();
            return;
        }

        if(!account.markable()) {
            result = Result.wrongAccess;
            return;
        }

        if(account.getId() == pd.id) {
            result = Result.cannotApplyToSelf;
            return;
        }

        int existingSession = Voting.processor.query(s -> (s.voting == main && s.args[1].equals(account.getId())));

        if(existingSession != -1) {
            Voter.game.run(id, args.length == 1 ? "y" : "n", "" + existingSession);
            return;
        }

        result = Result.redundant;
        if (args.length == 1) {
            if (account.isGriefer()) {
                return;
            }

            result = main.pushSession(pd, s -> {
                RankSetter.terminal.run("", args[0], "griefer");
            }, account.getName(), account.getId(), "[red]griefer[]");
        } else if(args[1].equals("unmark")) {
            if (!account.isGriefer()) {
                return;
            }

            result = main.pushSession(pd, s -> {
                RankSetter.terminal.run("", args[0], "newcomer");
            }, account.getName(), account.getId(), "[green]newcomer[]");
        } else {
            result = Result.wrongOption;
        }
    }

    public static VoteKick game = new VoteKick();
}
