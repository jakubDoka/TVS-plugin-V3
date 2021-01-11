package twp.database;

import arc.util.Time;
import twp.database.core.Raw;
import twp.database.enums.RankType;
import twp.database.enums.Stat;
import org.bson.Document;
import twp.tools.Text;

import static twp.Main.*;

// Account is player account handle, its used more to withdraw data
public class Account extends Raw {


    public static Account getNew(Document document){
        if (document == null) return null;
        return new Account(document);
    }

    public Account(Document data){
        this.data = data;
    }

    public boolean isProtected() {
        return getPassword() != null;
    }

    public boolean isParalyzed() {
        return data.get("paralyzed") != null;
    }

    public Object getPassword() {
        return data.get("password");
    }

    public String getLink(){
        return (String) data.get("link");
    }

    public String getIp() {
        return (String) data.get("ip");
    }

    public String getTextColor() {
        return (String) data.get("textColor");
    }

    public String getUuid() {
        return (String) data.get("uuid");
    }

    public boolean admin() {
        return getRank(RankType.rank).admin;
    }

    public long getLatestActivity() {
        Long la = data.getLong("lastConnect");
        if (la == null) {
            return 0;
        }

        return la;
    }

    public Rank getRank(RankType type) {
        String rankName = (String) data.get(type.name());
        if (rankName == null) {
            // there is some corruption going on so this is needed
            db.handler.setRank(getId(), ranks.newcomer, RankType.rank);
            return ranks.newcomer;
        }
        return ranks.getRank(rankName, type);
    }

    public boolean isGriefer() {
        return getRank(RankType.rank) == ranks.griefer;
    }

    public boolean markable() {
        return getRank(RankType.rank) != ranks.candidate && getRank(RankType.rank) != ranks.admin;
    }

    public String summarize(Stat stat) {
        return String.format("[gray]ID: [yellow]%d[] NAME: [white]%s[] RANK: %s %s: [orange]%d[] ",
                getId(),
                getName(),
                getRank(RankType.rank).getSuffix(),
                stat.name().toUpperCase(),
                getStat(stat)
        );
    }

    public Object[] basicStats() {
        Object[] os = new Object[]{
                getId(),
                getName(),
                getRank(RankType.rank).getSuffix(),
                null,
                null,
                data.get("country"),
                Text.milsToTime(Time.timeSinceMillis(getLatestActivity())),
        };

        for(int i = 1; i < RankType.values().length; i++) {
            Rank s = getRank(RankType.values()[i]);
            if (s != ranks.newcomer) {
                os[i+2] = s.getSuffix();
            } else {
                os[i+2] = "none";
            }
        }

        return os;
    }

    public Object[] stats() {
        Object[] os = new Object[Stat.values().length];

        for(int i = 0; i < Stat.values().length; i++) {
            Stat s = Stat.values()[i];
            if (s == Stat.age) {
                os[i] = Text.milsToTime(Time.timeSinceMillis(getStat(s)));
                continue;
            }
            if (s.time) {
                os[i] = Text.milsToTime(getStat(s));
            } else {
                os[i] = getStat(s);
            }
        }

        return os;
    }

}
