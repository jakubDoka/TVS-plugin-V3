package twp.commands;

import arc.util.Log;
import twp.database.*;
import twp.tools.Testing;

import static twp.Main.db;
import static twp.Main.ranks;

public abstract class RankSetter extends Command {
    public boolean freeAccess = false;

    public abstract boolean verification(String id);

    public RankSetter() {
        name = "setrank";
        argStruct = "<id/name> <rank>";
        description = "Sets rank of players, name can be used if player is online.";
    }

    @Override
    public void run(String[] args, String id) {
        // Verify - place dependent
        if (!verification(id)) {
            result = Result.noPerm; // done
            return;
        }

        // Resolve rank type, tis also checks if rank exists
        Rank rank = ranks.buildIn.get(args[1]);
        Log.info(rank);
        if (rank == null) {
            arg = new Object[]{
                    ranks.rankList(RankType.rank),
            };
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
        arg = new Object[] {
                data.getRank(RankType.rank).getSuffix(),
                rank.getSuffix(),
        };

        db.handler.setRank(data.getId(), rank, RankType.rank);

        // if player is online kick him. I do not want to deal with bag prone code to change his rank manually.
        PD pd = db.online.get(data.getUuid());
        if (pd != null) {
            pd.kick("kick-rankChange", 0, rank.getSuffix());
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
