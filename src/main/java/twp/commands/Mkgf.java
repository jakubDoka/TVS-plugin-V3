package twp.commands;

import arc.util.Log;
import twp.database.PD;
import twp.database.Perm;
import twp.database.Raw;
import twp.democracy.Voting;

import java.util.ArrayList;

import static twp.Main.db;

public class Mkgf extends Command {
    Voting main = new Voting(this, "main", 5, 2) {
        {
            protection = Perm.antiGrief;
        }
    };

    public Mkgf() {
        name = "mkgf";
        argStruct = "<mark/unmark> <id/name>";
        description = "It works like votekick but it also marks player a griefer witch means he can only spectate."; // Todo
    }

    @Override
    void run(String id, String... args) {
        PD pd = db.online.get(id);

        if(cannotInteract(id)) {
            return;
        }

        Raw raw = db.findData(args[1]);

        if(raw == null) {
            playerNotFound();
            return;
        }

        if(!raw.markable()) {
            result = Result.wrongAccess;
            return;
        }

        if(raw.getId() == pd.id) {
            result = Result.cannotApplyToSelf;
            return;
        }

        int existingSession = Voting.processor.query(s -> (s.voting == main && s.args[1].equals(raw.getId())));

        if(existingSession != -1) {
            result = Voter.game.use(id, args[0].equals("mark") ? "y" : "n", "" + existingSession);
            return;
        }

        result = Result.redundant;
        switch (args[0]) {
            case "mark":
                if(raw.isGriefer()) {
                    return;
                }

                result = main.pushSession(pd, s -> {
                    RankSetter.terminal.use("", args[1], "griefer");
                }, raw.getName(), raw.getId(), "[red]griefer[]");
                break;
            case "unmark":
                if(!raw.isGriefer()) {
                    return;
                }

                result = main.pushSession(pd, s -> {
                    RankSetter.terminal.use("", args[1], "newcomer");
                }, raw.getName(), raw.getId(), "[green]newcomer[]");
                break;
            default:
                result = Result.wrongOption;
        }
    }

    public static Mkgf
                game = new Mkgf(),
                votekick = new Mkgf(){{name = "votekick";}};
}
