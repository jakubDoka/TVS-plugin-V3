package twp.commands;

import arc.Core;
import twp.database.Account;
import twp.database.PD;
import twp.database.Rank;
import twp.database.enums.RankType;

import static twp.Main.*;

// Rank setter lets admins to change build-in ranks of players
// Its designed for use on multiple places (terminal, game, discord)
public class RankSetter extends Command {

    public RankSetter() {
        name = "setrank";
        argStruct = "<id/name> <rank>";
        description = "Sets rank of players, name can be used if player is online.";
    }

    @Override
    public void run(String id, String ...args) {
        // Verify - place dependent
        if (!verifier.verify(id)) {
            result = Result.noPerm; // done
            return;
        }

        // Resolve rank type, tis also checks if rank exists
        Rank rank = ranks.buildIn.get(args[1]);
        if (rank == null) {
            setArg(ranks.rankList(RankType.rank));
            result = Result.wrongRank; // done
            return;
        }

        // Search target
        Account data = db.findAccount(args[0]);
        if (data == null) {
            playerNotFound();
            return;
        }

        // admin rank can be set only through terminal
        if (!freeAccess && (rank.admin || data.admin())) {
            result = Result.wrongAccess; // done
            return;
        }

        // setting arguments to show change
        setArg(data.getRank(RankType.rank).getSuffix(), rank.getSuffix());

        db.handler.setRank(data.getId(), rank, RankType.rank);

        // if player is online kick him. I do not want to deal with bag prone code to change his rank manually.
        PD pd = db.online.get(data.getUuid());
        if (pd != null && !testMode) {
            Core.app.post(() -> pd.kick("kick-rankChange", 0, rank.getSuffix()));
        }
    }

    public static RankSetter
            game = new RankSetter() {{verifier = this::isPlayerAdmin;}},
            terminal = new RankSetter();
}
