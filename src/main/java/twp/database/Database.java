package twp.database;

import arc.func.Cons;
import arc.util.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClients;
import twp.database.enums.Perm;
import twp.database.enums.RankType;
import twp.database.enums.Setting;
import twp.database.enums.Stat;
import twp.database.maps.MapHandler;
import twp.Global;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import org.bson.Document;
import twp.game.Loadout;
import twp.tools.Logging;
import twp.tools.Text;

import java.util.*;
import java.util.Map.Entry;

import static twp.Main.*;

// Main database interface
public class Database {
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
            "Prometeus",
            "Gerald"
    };

    // mongo stuff
    public MongoClient client;
    public MongoDatabase database;
    MongoCollection<Document> rawData;
    MongoCollection<Document> rawMapData;

    // handler has works directly with database
    public AccountHandler handler;
    public MapHandler maps;
    public Loadout loadout;

    // online player are here by their ids
    public HashMap<String, PD> online = new HashMap<>();

    public Database(){
        Logging.on(EventType.PlayerConnect.class, e-> {
            validateName(e.player);

            PD pd = handler.loadData(new DBPlayer(e.player));
            online.put(e.player.uuid(), pd);

            pd.updateName();

            bundle.resolveBundle(pd);

            if (!pd.cannotInteract()) checkAchievements(pd, handler.getAccount(pd.id));
        });

        Logging.on(EventType.PlayerLeave.class, e-> {
            cleanupOnlineList();
        });

        Logging.on(EventType.WithdrawEvent.class, e-> {
            PD pd = online.get(e.player.uuid());
            if(pd != null) {
                handler.inc(pd.id, Stat.itemsTransported, e.amount);
            }
        });

        Logging.on(EventType.DepositEvent.class, e-> {
            PD pd = online.get(e.player.uuid());
            if(pd != null) {
                handler.inc(pd.id, Stat.itemsTransported, e.amount);
            }
        });

        Logging.on(EventType.BlockBuildEndEvent.class, e -> {
            if(!e.unit.isPlayer() || e.tile.block().buildCost/60<1) return;

            PD pd = online.get(e.unit.getPlayer().uuid());
            if(pd != null) {
                if (e.breaking) {
                    handler.inc(pd.id, Stat.buildingsBroken, 1);
                } else {
                    handler.inc(pd.id, Stat.buildingsBuilt, 1);
                }
            }
        });

        Logging.on(EventType.UnitDestroyEvent.class, e -> {
            if(e.unit.isPlayer()) {
                PD pd = online.get(e.unit.getPlayer().uuid());
                if(pd != null) {
                    handler.inc(pd.id, Stat.deaths, 1);
                }
            }

            for(Player p : Groups.player) {
                if(p.team() != e.unit.team()) {
                    PD pd = online.get(p.uuid());
                    if(pd != null) {
                        handler.inc(pd.id, Stat.enemiesKilled, 1);
                    }
                }
            }
        });

        Logging.on(EventType.GameOverEvent.class, e-> {
            for(Player p : Groups.player) {
                PD pd = online.get(p.uuid());
                if (pd == null) continue;
                if(p.team() == e.winner) {
                    handler.inc(pd.id, Stat.gamesWon, 1);
                }
                handler.inc(pd.id, Stat.gamesPlayed, 1);
            }
        });

        reconnect();
    }

    public void reconnect() {
        client = MongoClients.create(config.db.address);
        database = client.getDatabase(config.db.name);
        rawData = database.getCollection(config.db.players);
        rawMapData = database.getCollection(config.db.maps);
        handler = new AccountHandler(rawData, database.getCollection(counter));
        maps = new MapHandler(rawMapData, database.getCollection(mapCounter));
        loadout = new Loadout(database.getCollection(config.loadout.name));
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

    public long getSize() {
        return rawData.estimatedDocumentCount();
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

    public Account findAccount(String target){
        PD p = null;
        for(PD pd : online.values()){
            if(!pd.isInvalid() && (pd.player.name.equals(target) || pd.player.p.name.equalsIgnoreCase(target))){
                p = pd;
            }
        }

        if(p != null){
            return p.getAccount();
        }

        if(Strings.canParsePositiveInt(target)){
            return handler.getAccount(Long.parseLong(target));
        }

        return null;
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
        if (!originalName.equals(player.name) || player.name.length() > config.maxNameLength) {
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

    public void cleanupOnlineList() {
        for(Iterator<Entry<String, PD>> iter = db.online.entrySet().iterator(); iter.hasNext(); ){
            PD pd = iter.next().getValue();
            if(pd.isInvalid()) {
                iter.remove();
                continue;
            }
            
            if(pd.disconnected()) {
                db.handler.free(pd);
                iter.remove();
                continue;
            }
        }
    }
}
