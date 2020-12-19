package twp.commands;

import twp.database.PD;
import twp.database.enums.Perm;
import twp.database.Account;
import twp.democracy.Voting;

import static twp.Main.db;

public class Mkgf extends Command {
    Voting main = new Voting(this, "main", 5, 2) {
        {
            protection = Perm.antiGrief;
        }
    };

    public Mkgf() {
        name = "mkgf";
        argStruct = "<id/name> [unmark]";
        description = "It works like votekick but it also marks player a griefer witch means he can only spectate."; // Todo
    }

    @Override
    public void run(String id, String... args) {
        PD pd = db.online.get(id);

        if(cannotInteract(id)) {
            return;
        }

        Account account = db.findData(args[0]);

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
                RankSetter.terminal.run("", args[0], "griefer", caller.player.name);
            }, account.getName(), account.getId(), "[red]griefer[]");
        } else if(args[1].equals("unmark")) {
            if (!account.isGriefer()) {
                return;
            }

            result = main.pushSession(pd, s -> {
                RankSetter.terminal.run("", args[0], "newcomer", caller.player.name);
            }, account.getName(), account.getId(), "[green]newcomer[]");
        } else {
            result = Result.wrongOption;
        }
    }

    public static Mkgf
                game = new Mkgf(),
                votekick = new Mkgf(){{name = "votekick";}};
}
