package main.database;

import org.bson.Document;

import static main.Main.ranks;

public class Raw {
    Document data;

    public static Raw getNew(Document document){
        if (document == null) return null;
        return new Raw(document);
    }

    public Raw(Document data){
        this.data = data;
    }

    public Long getStat( Stat stat) {
        return getStat(stat.name());
    }

    public Long getStat(String stat) {
        Long val = (Long) data.get(stat);
        return val == null ? 0 : val;
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

    public Long getPrevious() {
        return (Long) data.get("previous");
    }

    public Long getId() {
        return (Long) data.get("_id");
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
        return getRank(RankType.normal).admin;
    }

    public long getLatestActivity() {
        return (Long) data.get("lastActive");
    }

    public Rank getRank(RankType type) {
        String rankName = (String) data.get(type.name());
        if (rankName == null) {
            return null;
        }
        return ranks.getRank(rankName, type);
    }

    public String getName() {
        return (String) data.get("name");
    }

    public boolean isGriefer() {
        return getRank(RankType.normal) == ranks.griefer;
    }
}
