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
import twp.database.enums.Perm;
import twp.database.enums.RankType;
import twp.database.enums.Setting;
import twp.database.enums.Stat;
import twp.database.maps.MapHandler;
import twp.tools.Testing;
import twp.tools.Text;

import java.util.*;

import static twp.Main.bundle;
import static twp.Main.ranks;

public class Database {
    public final String playerCollection = "PlayerData";
    public static final String AFK = "[gray]<AFK>[]";
    public static final String counter = "counter";
    public static final String mapCounter = "mapCounter";
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
    MongoCollection<Document> rawMapData = database.getCollection(Global.config.mapCollection);

    // handler has works directly with database
    public DataHandler handler = new DataHandler(rawData, database.getCollection(counter));
    public MapHandler maps = new MapHandler(rawMapData, database.getCollection(mapCounter));

    // online player are here by their ids
    public SyncMap<String, PD> online = new SyncMap<>();

    public Database(){
        Events.on(EventType.PlayerConnect.class,e-> {

            validateName(e.player);

            PD pd = handler.loadData(new DBPlayer(e.player));
            online.put(e.player.uuid(), pd);

            pd.updateName();

            bundle.resolveBundle(pd);

            if (!pd.cannotInteract()) checkAchievements(pd, handler.getAccount(pd.id));
        });

        Events.on(EventType.PlayerLeave.class,e->{
            PD pd = online.get(e.player.uuid());
            if(pd == null) {
                Testing.Log("player left without ewen being present");
                return;
            }
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

    // function checks whether player can obtain any rank ON THREAD and gives him that
    public void checkAchievements(PD pd, Account doc) {
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

    public Account findData(String target) {
        Account res = null;
        if(Strings.canParsePositiveInt(target)) {
            res = handler.getAccount(Long.parseLong(target));
        }
        if (res != null) {
            return res;
        }
        PD p = online.find((pd) -> {
            if(pd.isInvalid()) {
                return false;
            }
            return pd.player.name.equals(target) || pd.player.p.name.equalsIgnoreCase(target);
        });

        if(p != null) {
            return p.getAccount();
        }
        return null;
    }


    public int getDatabaseSize(){
        return (int) database.runCommand(new Document("collStats", playerCollection)).get("count");
    }

    public void disconnectAccount(PD pd){
        if(pd.paralyzed) return;
        if(!pd.getAccount().isProtected()) {
            handler.delete(pd.id);
        } else {
            handler.setUuid(pd.id, "why cant i just die");
            handler.setIp(pd.id, "because you are too week");
        }
    }

    static String docToString(Document doc) {
        Account d = Account.getNew(doc);
        return "[gray][yellow]" + d.getId() + "[] | " + d.getName() + " | []" + d.getRank(RankType.rank).getSuffix() ;

    }

    //removes fake ranks and colors
    public void validateName(Player player) {
        String originalName = player.name;
        player.name = Text.cleanName(player.name);
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
        private final Map<K, V> map = Collections.synchronizedMap(new HashMap<K, V>());

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

        public synchronized void forEachKey(Cons<Iterator<K>> con) {
            Iterator<K> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                con.get(iter);
            }
        }

        public synchronized void forEachValue(Cons<Iterator<V>> con) {
            Iterator<V> iter = map.values().iterator();
            while (iter.hasNext()) {
                con.get(iter);
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
