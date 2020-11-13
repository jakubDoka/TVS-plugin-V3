package twp.database;

import arc.util.Time;
import mindustry.gen.Player;
import twp.Main;


import javax.swing.text.PlainDocument;
import java.util.HashSet;
import java.util.ResourceBundle;

import static twp.Main.db;
import static twp.Main.ranks;

public class PD{
    private static final String prefix = "[coral][[[scarlet]Server[]]:[#cbcbcb] ";

    public DBPlayer player;
    public String textColor;

    public Rank dRank, rank;
    private Rank sRank;

    private final HashSet<Rank> obtained = new HashSet<>();
    private final HashSet<Perm> perms = new HashSet<>();

    public boolean afk, paralyzed;

    public long id;

    public long lastAction;
    public long lastMessage;
    public long joined = lastAction = lastMessage = Time.millis();
    private ResourceBundle bundle;

    //public ResourceBundle bundle = Bundle.defaultBundle;
    public String locString = "en_US";

    public PD() {}


    public PD(DBPlayer player, Raw doc) {
        this.player = player;
        rank = doc.getRank(RankType.rank);
        textColor = doc.getTextColor();
        id = doc.getId();
        addRank(rank);
    }

    public static PD makeParalyzed(DBPlayer p) {
        return new PD(){{
            player = p;
            paralyzed = true;
            rank = ranks.paralyzed;
            id = DataHandler.paralyzedId;
        }};
    }

    public Raw getDoc() {
        return db.handler.getDoc(id);
    }

    public void updateName() {
        String orig = player.name;
        Player player = this.player.p;
        if (afk) {
            player.name = orig + "[gray]<AFK>[]";
        } else if (dRank != null && dRank.displayed) {
            player.name = orig + dRank.suffix();
        } else if (sRank != null && sRank.displayed) {
            player.name = orig + sRank.suffix();
        } else if (rank != null){
            player.name = orig + rank.suffix();
        }
        if (rank != null) {
            player.admin = rank.admin;
        }
    }

    public synchronized void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public synchronized void sendServerMessage(String message, Object ... args) {
        if(player.p == null) {
            new RuntimeException("sending message to PD with a null player").printStackTrace();
            return;
        }
        if(bundle != null && bundle.containsKey(message)) {
            player.p.sendMessage(prefix + String.format(bundle.getString(message), args));
        } else {
            player.p.sendMessage(prefix + String.format(Main.bundle.getDefault(message), args));
        }
    }

    public void sendMessage(String message) {
        if(player.p == null) {
            new RuntimeException("sending message to PD with a null player").printStackTrace();
            return;
        }
        player.p.sendMessage(message);
    }

    public void kick(String message, int duration, Object ... args) {
        if(player.p == null) {
            new RuntimeException("attempting to disconnect PD with a null player").printStackTrace();
            return;
        }
        if(bundle != null && bundle.containsKey(message)) {
            player.p.con.kick(String.format(bundle.getString(message), args), duration);
        } else {
            player.p.con.kick(String.format(Main.bundle.getDefault(message), args), duration);
        }
    }

    public synchronized boolean hasThisPerm(Perm perm) {
        return !(paralyzed || !perms.contains(perm));
    }

    public synchronized boolean hasPermLevel(Perm perm) {
        return hasPermLevel(perm.value);
    }

    public synchronized boolean hasPermLevel(int level) {
        if(paralyzed) return false;
        for(Perm p : perms) {
            if (p.value >= level) return true;
        }
        return false;
    }

    public boolean isGriefer() {
        return rank == ranks.griefer || paralyzed;
    }

    public void onAction() {
        lastAction = Time.millis();
        if(!afk) return;
        afk = false;
        updateName();
    }

    public synchronized int getHighestPermissionLevel() {
        int highest = -1;
        for( Perm p : perms) {
            if(p.value > highest) highest = p.value;
        }
        return highest;
    }



    public synchronized long getPlayTime() {
        return db.handler.getStat(id,Stat.playTime.name()) + Time.timeSinceMillis(joined);
    }

    public synchronized void addRank(Rank rank) {
        obtained.add(rank);
        addPerms(rank);
    }

    public synchronized void addPerms(Rank rank) {
        if(rank.permissions == null) return;
        for(String p : rank.permissions) {
            perms.add(Perm.valueOf(p));
        }
    }

    public synchronized void removeRank(Rank rank) {
        obtained.remove(rank);
        for (String s : rank.permissions) {
            perms.remove(Perm.valueOf(s));
        }
    }

    public synchronized boolean hasObtained(Rank r) {
        return obtained.contains(r);
    }

    public synchronized void setSpecialRank(Rank r) {
        sRank = r;
    }

    public synchronized Rank getSpacialRank() {
        return sRank;
    }
}
