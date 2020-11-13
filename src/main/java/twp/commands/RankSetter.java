package twp.commands;

import twp.database.*;

import static twp.Main.db;
import static twp.Main.ranks;

public abstract class RankSetter extends Command {
    public String wrongRank = "wrongRank", wrongAccess = "wrongAccess";
    public boolean freeAccess = false;

    public abstract boolean verification(String id);

    public RankSetter() {
        name = "setrank";
        argStruct = "<id/name> <rank>";
        description = "Sets rank of players, name can be used if player is online.";
    }

    @Override
    public String run(String[] args, String id) {
        // Verify - place dependent
        if (!verification(id)) {
            return noPerm;
        }

        // Resolve rank type, tis also checks if rank exists
        RankType rankType = ranks.rankType(args[1]);
        if (rankType == null) {
            return wrongRank;
        }

        // Search target
        Raw data = db.findData(args[0]);
        if (data == null) {
            return notFound;
        }

        // admin rank can be set only through terminal
        Rank rank = ranks.getRank(args[1], rankType);
        if (!freeAccess && (rank.admin || data.admin())) {
            return wrongAccess;
        }

        db.handler.setRank(data.getId(), rank, rankType);

        // if player is online kick him. I do not want to deal with bag prone code to change his rank manually.
        PD pd = db.online.get(data.getUuid());
        if (pd != null) {
            if (pd.player.p == null) {
                new RuntimeException("player is in online but pd.player.p is null").printStackTrace();
                return success;
            }
            pd.kick("kick-rankChange", 0);
        }

        return success;
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
