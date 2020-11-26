package twp.database;

import arc.util.Time;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;

import mindustry.gen.Player;
import org.bson.Document;
import org.bson.conversions.Bson;
import twp.database.core.Handler;
import twp.database.enums.RankType;
import twp.database.enums.Setting;
import twp.database.enums.Stat;


import static com.mongodb.client.model.Filters.and;
import static twp.Main.ranks;

public class DataHandler extends Handler {
    public final static long paralyzedId = -1;
    public final static long invalidId = -2;

    enum Indexed {
        uuid,
        ip,
        discordLink
    }

    public DataHandler(MongoCollection<Document> data, MongoCollection<Document> counter){
        super(data, counter);

        // Initializing indexes
        for(Indexed i : Indexed.values()) {
            data.createIndex(Indexes.descending(i.name()));
        }

        // If there isn't paralyzed already, add it
        Account doc = getAccount(paralyzedId);
        if(doc == null) {
            data.insertOne(new Document("_id", paralyzedId));
        }
    }

    // utility filter methods

    public Bson playerFilter(DBPlayer player){
        return Filters.and(uuidFilter(player.uuid), ipFilter(player.ip));
    }

    public Bson playerOrFilter(DBPlayer player){
        return Filters.or(uuidFilter(player.uuid), ipFilter(player.ip));
    }

    public Bson uuidFilter(String uuid) {
        return Filters.eq("uuid", uuid);
    }

    public Bson ipFilter(String ip){
        return Filters.eq("ip", ip);
    }

    // Returns raw document holding players account
    public Account getAccount(long id) {
        return Account.getNew(data.find(idFilter(id)).first());
    }

    public Account getDocByDiscordLink(String link) {
        return Account.getNew(data.find(Filters.eq("discordLink", link)).first());
    }

    public void setUuid(long id, String uuid) {
        set(id, "uuid", uuid);
    }

    public void setIp(long id, String ip) {
        set(id, "ip", ip);
    }


    public void addToSet(long id, String field, Object value) {
        data.updateOne(idFilter(id), Updates.addToSet(field, value));
    }

    public long getPlace(Account doc, String stat){
        long res = 0;
        for(Document d: gt(doc.data, stat)){
            res++;
        }
        return res;
    }

    public Rank getRank(long id, RankType type) {
        String rankName = (String) get( id, type.name());
        if (rankName == null) {
            return null;
        }
        return ranks.getRank(rankName, type);
    }

    public void removeRank(long id, RankType type) {
        unset( id, type.name());
    }

    public void setRank(long id, Rank rank, RankType type) {
        data.updateOne(idFilter(id), Updates.set(type.name(), rank.name));
    }

    public void free(PD pd) {
        long id = pd.id;
        set(id, "textColor", pd.textColor);
        inc(id, Stat.playTime, Time.timeSinceMillis(pd.joined));
        set(id, "lastActive", Time.millis());
        Account doc = getAccount(pd.id);
        if (doc == null) {
            return;
        }
        // add setting level

        if (pd.dRank != null) set(id, RankType.donationRank.name(), pd.dRank.name);
        else unset(id, RankType.donationRank.name());
        if (pd.getSpacialRank() != null) set(id, RankType.specialRank.name(), pd.getSpacialRank().name);
        else unset(id, RankType.specialRank.name());
    }

    // LoadData finds players account, if there is none it creates new,
    // if found account ip paralyzed it returns paralyzed data
    public PD loadData(DBPlayer player) {
        Account doc = findData(player);
        if(doc == null) {
            doc = makeNewAccount(player.uuid, player.ip);
        } else if(doc.isParalyzed()) {
            return PD.makeParalyzed(player);
        }
        PD pd = new PD(player, doc);
        set(pd.id, "name", player.name);
        return pd;
    }

    // findData searches for player data, it can return null if account does not exist or paralyzed account
    // if there are some account that fit at least with ip or uuid
    public Account findData(DBPlayer player) {
        Document cnd = data.find(playerFilter(player)).first();
        if (cnd != null) {
            return Account.getNew(cnd);
        }

        boolean exists = false;
        for (Document d : data.find(playerOrFilter(player))) {
            exists = true;
            Account doc = Account.getNew(d);
            if (doc.isProtected()) continue;
            return doc;
        }

        if (!exists) {
            // TODO write related message to player, something like "no match found if you are old player pleas log in with command..."
            return null;
        }


        // TODO inform playe that he is paralyzed
        return Account.getNew(new Document("paralyzed", true));
    }

    // Bind binds player to an account so he automatically logs to it
    public void bind(Player player, long id) {
        setUuid(id, player.uuid());
        setIp(id, player.con.address);
    }

    // creates account with all settings enabled
    // newcomer rank and sets bord date
    public Account makeNewAccount(String uuid, String ip){
        long id = newId();
        data.insertOne(new Document("_id", id));
        for(Setting s :Setting.values()) {
            addToSet(id, "settings", s.name());
        }
        setUuid(id, uuid);
        setIp(id, ip);
        setRank(id, ranks.newcomer, RankType.rank);
        setStat(id, Stat.age, Time.millis());

        return getAccount(id);
    }

    // returns formatted string of suggested accounts that share ip or uuid with player
    public String getSuggestions(String uuid, String ip) {
        StringBuilder sb = new StringBuilder("[yellow]");
        FindIterable<Document> fits = data.find(Filters.or(uuidFilter(uuid), ipFilter(ip)));
        for(Document fit : fits) {
            Account doc = Account.getNew(fit);
            sb.append(doc.getName()).append("[gray] || []").append(doc.getId()).append("\n");
        }
        return sb.toString();
    }
}

