package main.database;

import arc.util.Time;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;

import mindustry.gen.Player;
import org.bson.Document;
import org.bson.conversions.Bson;


import static com.mongodb.client.model.Filters.and;
import static main.Main.ranks;

public class DataHandler {
    MongoCollection<Document> data;
    MongoCollection<Document> counter;

    public final static long paralyzedId = -1;
    public final static long invalidId = -2;



    enum Indexed {
        uuid,
        ip,
        discordLink
    }



    public DataHandler(MongoCollection<Document> data, MongoCollection<Document> counter){
        this.data = data;
        this.counter = counter;

        // Initializing indexes
        for(Indexed i : Indexed.values()) {
            data.createIndex(Indexes.descending(i.name()));
        }

        // If there isn't paralyzed already, add it
        Raw doc = getDoc(paralyzedId);
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

    public Bson idFilter(long id){
        return Filters.eq("_id", id);
    }

    // Returns raw document holding players account
    public Raw getDoc(long id) {
        return Raw.getNew(data.find(idFilter(id)).first());
    }

    public Raw getDocByDiscordLink(String link) {
        return Raw.getNew(data.find(Filters.eq("discordLink", link)).first());
    }

    public void delete(long id){
        data.deleteOne(idFilter(id));
    }

    public void set(long id, String field, Object value) {
        data.updateOne(idFilter(id), Updates.set(field, value));
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

    public void pull(long id, String field, Object value) {
        data.updateOne(idFilter( id), Updates.pull(field, value));
    }

    public boolean contains(long id, String field, Object value) {
        return data.find(and(idFilter( id), Filters.eq(field, value))).first() != null;
    }


    public Object get(long id, String field) {
        Document dc = data.find(idFilter( id)).first();
        if (dc == null) return null;
        return dc.get(field);
    }

    public void inc(long id, Stat stat, long amount){
        data.updateOne(idFilter( id), Updates.inc(stat.name(), amount));
    }

    public void incOne(long id, Stat stat) {
        inc( id, stat, 1);
    }

    public Long getStat(long id, String stat) {
        Long val = (Long) get( id, stat);
        return val == null ? 0 : val;
    }

    public FindIterable<Document> gt(Document doc, String stat) {
        return data.find(Filters.gt(stat, doc.get(stat)));
    }

    public long getPlace(Raw doc, String stat){
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

    public void remove(long id, String field) {
        data.updateOne(idFilter(id), Updates.unset(field));
    }

    public void removeRank(long id, RankType type) {
        remove( id, type.name());
    }

    public void setRank(String ip, Rank rank, RankType type) {
        data.updateOne(ipFilter(ip), Updates.set(type.name(), rank.name));
    }

    public void setRank(long id, Rank rank, RankType type) {
        data.updateOne(idFilter(id), Updates.set(type.name(), rank.name));
    }

    public void free(PD pd) {
        long id = pd.id;
        set(id, "textColor", pd.textColor);
        inc(id, Stat.playTime, Time.timeSinceMillis(pd.joined));
        set(id, "lastActive", Time.millis());
        Raw doc = getDoc(pd.id);
        if (doc == null) {
            return;
        }
        // add setting level

        if (pd.dRank != null) set(id, RankType.donation.name(), pd.dRank.name);
        else remove(id, RankType.donation.name());
        if (pd.getSpacialRank() != null) set(id, RankType.special.name(), pd.getSpacialRank().name);
        else remove(id, RankType.special.name());
    }

    // LoadData finds players account, if there is none it creates new,
    // if found account ip paralyzed it returns paralyzed data
    public PD loadData(DBPlayer player) {
        Raw doc = findData(player);
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
    public Raw findData(DBPlayer player) {
        Document cnd = data.find(playerFilter(player)).first();
        if (cnd != null) {
            return Raw.getNew(cnd);
        }

        boolean exists = false;
        for (Document d : data.find(playerFilter(player))) {
            exists = true;
            Raw doc = Raw.getNew(d);
            if (doc.isProtected()) continue;
            return doc;
        }

        if (!exists) {
            // TODO write related message to player, something like "no match found if you are old player pleas log in with command..."
            return null;
        }


        // TODO inform playe that he is paralyzed
        return Raw.getNew(new Document("paralyzed", true));
    }

    // Bind binds player to an account so he automatically logs to it
    public void bind(Player player, long id) {
        setUuid(id, player.uuid());
        setIp(id, player.con.address);
    }

    // creates account with all settings enabled
    // newcomer rank and sets bord date
    public Raw makeNewAccount(String uuid, String ip){
        long id = newId();
        data.insertOne(new Document("_id", id));
        for(Setting s :Setting.values()) {
            addToSet(id, "settings", s.name());
        }
        setUuid(id, uuid);
        setIp(id, ip);
        setRank(id, ranks.newcomer, RankType.normal);
        set(id, "age", Time.millis());

        return getDoc(id);
    }

    // newID creates new incremented id
    public long newId() {
        if(counter.updateOne(idFilter(0), Updates.inc("id", 1)).getModifiedCount() == 0){
            long id = 0;
            Document latest = data.find().sort(new Document("_id", -1)).first();
            if(latest != null) {
                id = (long)latest.get("_id");
                if (id == -1) {
                   id = 0;
                }
            }
            counter.insertOne(new Document("_id", 0).append("id",id));
        }
        Document counter = this.counter.find().first();
        if(counter == null){
            throw new IllegalStateException("Well then this is fucked.");
        }
        return (long) counter.get("id");
    }

    // returns formatted string of suggested accounts that share ip or uuid with player
    public String getSuggestions(String uuid, String ip) {
        StringBuilder sb = new StringBuilder("[yellow]");
        FindIterable<Document> fits = data.find(Filters.or(uuidFilter(uuid), ipFilter(ip)));
        for(Document fit : fits) {
            Raw doc = Raw.getNew(fit);
            sb.append(doc.getName()).append("[gray] || []").append(doc.getId()).append("\n");
        }
        return sb.toString();
    }

    // For testing purposes
    public void drop() {
        data.drop();
        counter.drop();
    }
}

