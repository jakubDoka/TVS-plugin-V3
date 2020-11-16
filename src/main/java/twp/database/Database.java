package twp.database;

import arc.Events;
import arc.func.Cons;
import arc.util.Strings;
import arc.util.Time;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClients;
import twp.Global;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.type.ItemStack;
import org.bson.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static twp.Global.cleanName;
import static twp.Main.bundle;
import static twp.Main.ranks;

public class Database {
    public final String playerCollection = "PlayerData";
    public static final String AFK = "[gray]<AFK>[]";
    public static final String counter = "counter";
    static final String subnetFile = Global.save_dir + "subnetBuns.json";
    static final String cpnFile = Global.save_dir + "detectedVpn.json";

    // Random name replacements, you can add some fun ones in pr
    static String[] names = new String[]{
            "Steve",
            "Herold",
            "Jakub",
            "Socrates",
            "Poneklicean",
            "Orfeus",
            "Euridica",
            "Svorad",
            "Metod",
            "Ezechiel",
            "Arlong",
            "Luffy",
    };

    // mongo stuff
    public MongoClient client = MongoClients.create(Global.config.dbAddress);
    public MongoDatabase database = client.getDatabase(Global.config.dbName);
    MongoCollection<Document> rawData = database.getCollection(Global.config.playerCollection);

    // handler has works directly with database
    public DataHandler handler = new DataHandler(rawData, database.getCollection(counter));

    // online player are here by their ids
    public SyncMap<String, PD> online = new SyncMap<>();

    public Database(){
        Events.on(EventType.PlayerConnect.class,e-> {

            validateName(e.player);

            PD pd = handler.loadData(new DBPlayer(e.player));
            online.put(e.player.uuid(), pd);

            pd.updateName();

            bundle.resolveBundle(pd);

            if (!pd.isGriefer()) checkAchievements(pd, handler.getDoc(pd.id));
        });

        Events.on(EventType.PlayerLeave.class,e->{
            PD pd = online.get(e.player.uuid());
            if(pd == null) throw new RuntimeException("player left without ewen being present");

            online.remove(e.player.uuid());
            handler.free(pd);

        });

        Events.on(EventType.WithdrawEvent.class, e-> {
            PD pd = online.get(e.player.uuid());
            if (pd == null) {
                return;
            }
            handler.inc(pd.id, Stat.itemsTransported, e.amount);
        });

        Events.on(EventType.DepositEvent.class, e-> {
            PD pd = online.get(e.player.uuid());
            if (pd == null) {
                return;
            }
            handler.inc(pd.id, Stat.itemsTransported, e.amount);
        });

    }

    private static ItemStack[] multiplyReq(ItemStack[] requirements, int multiplier) {
        ItemStack[] res = new ItemStack[requirements.length];
        for(int i = 0; i < res.length; i++){
            res[i] = new ItemStack(requirements[i].item, requirements[i].amount * multiplier);
        }
        return res;
    }


    // function checks whether player can obtain any rank ON THREAD and gives him that
    public void checkAchievements(PD pd, Raw doc) {
        for(Rank r : ranks.special.values()) {
            pd.removeRank(r);
            pd.setSpecialRank(null);
        }
        new Thread(()->{
            for(Rank rank : ranks.special.values()){
                if(rank.condition(doc,pd)){
                    if (pd.getSpacialRank() == null || pd.getSpacialRank().value < rank.value) {
                        pd.setSpecialRank(rank);
                    }
                }
            }
            synchronized (pd){
                pd.updateName();
            }
        }).start();
    }

    public boolean hasDisabled(long id, Perm perm) {
        return handler.contains(id, "settings", perm.name());
    }

    public boolean hasEnabled(long id, Setting setting) {
        return handler.contains(id, "settings", setting.name());
    }

    public boolean hasMuted(long id, String other){
        return handler.contains(id, "mutes", other);
    }

    //just for testing purposes
    public void clear(){
        rawData.drop();
        reconnect();
    }

    public void reconnect() {
        client = MongoClients.create(Global.config.dbAddress);
        database = client.getDatabase(Global.config.dbName);
        rawData = database.getCollection(playerCollection);
        handler = new DataHandler(rawData, database.getCollection(counter));
    }

    public Raw findData(String target) {
        Raw res = null;
        if(Strings.canParsePositiveInt(target)) {
            res = handler.getDoc(Long.parseLong(target));
        }
        if (res != null) {
            return res;
        }

        return online.find((pd) -> pd.player.name.equals(target)).getDoc();
    }


    public int getDatabaseSize(){
        return (int) database.runCommand(new Document("collStats", playerCollection)).get("count");
    }

    public void disconnectAccount(PD pd){
        if(pd.paralyzed) return;
        if(!pd.getDoc().isProtected()) {
            handler.delete(pd.id);
        } else {
            handler.setUuid(pd.id, "why cant i just die");
            handler.setIp(pd.id, "because you are too week");
        }
    }

    static String docToString(Document doc) {
        Raw d = Raw.getNew(doc);
        return "[gray][yellow]" + d.getId() + "[] | " + d.getName() + " | []" + d.getRank(RankType.rank).getSuffix() ;

    }

    //removes fake ranks and colors
    public void validateName(Player player) {
        String originalName = player.name;
        player.name = cleanName(player.name);
        if (!originalName.equals(player.name)) {
            //name cannot be blank so then replace it with some random name
            if (player.name.replace(" ", "").isEmpty()) {
                player.name = pickFreeName();
            }
        }
    }

    public String pickFreeName() {
        for (String n : names){
            if (Groups.player.find(p -> p.name.equals(n)) == null) {
                return n;
            }
        }

        return String.valueOf(Time.millis());
    }


    public static class SyncMap<K, V> {
        private Map<K, V> map = Collections.synchronizedMap(new HashMap<K, V>());

        public synchronized void put(K key, V value) {
            map.put(key, value);
        }

        public synchronized V get(K key) {
            return map.get(key);
        }

        public synchronized V remove(K key) {
            return map.remove(key);
        }

        public synchronized boolean isEmpty() {
            return map.isEmpty();
        }

        public synchronized boolean containsKey(K key) {
            return map.containsKey(key);
        }

        public synchronized void forEachKey(Cons<K> con) {
            for(K key : map.keySet()) {
                con.get(key);
            }
        }

        public synchronized void forEachValue(Cons<V> con) {
            for(V val : map.values()) {
                con.get(val);
            }
        }

        public synchronized V find(Filter<V> con) {
            for(V val : map.values()) {
                if(con.run(val)) {
                    return val;
                }
            }
            return null;
        }

        interface Filter<T> {
            boolean run(T val);
        }
    }
}
