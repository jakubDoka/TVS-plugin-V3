package twp.database;

import org.bson.Document;
import twp.database.core.Raw;
import twp.database.enums.RankType;
import twp.database.enums.Stat;

import static twp.Main.*;

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
        Long la = data.getLong("lastActive");
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


}
