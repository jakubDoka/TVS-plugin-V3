package twp.commands;

import arc.Core;
import twp.database.PD;
import twp.database.Rank;
import twp.database.RankType;
import twp.database.Raw;

import static twp.Main.db;
import static twp.Main.ranks;

// Rank setter lets admins to change build-in ranks of players
// Its designed for use on multiple places (terminal, game, discord)
public abstract class RankSetter extends Command {
    public boolean freeAccess = false;

    // verification is context dependent
    public abstract boolean verification(String id);

    public RankSetter() {
        name = "setrank";
        argStruct = "<id/name> <rank>";
        description = "Sets rank of players, name can be used if player is online.";
    }

    @Override
    public void run(String id, String ...args) {
        // Verify - place dependent
        if (!verification(id)) {
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
        Raw data = db.findData(args[0]);
        if (data == null) {
            result = Result.playerNotFound; // done
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
        if (pd != null) {
            Core.app.post(() -> pd.kick("kick-rankChange", 0, rank.getSuffix()));
        }

        result = Result.success; // done
    }

    public static RankSetter game = new RankSetter() {
        @Override
        public boolean verification(String id) {
            PD data = db.online.get(id);
            return data != null && data.rank.admin;
        }
    };

    public static RankSetter terminal = new RankSetter() {
        {
            freeAccess = true;
        }

        @Override
        public boolean verification(String id) {
            return true;
        }
    };
}
