package main.database;

import arc.util.Log;
import arc.util.Time;
import mindustry.gen.Player;


import java.util.HashSet;

import static main.Main.db;
import static main.Main.ranks;

public class PD{
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

    //public ResourceBundle bundle = Bundle.defaultBundle;
    public String locString = "en_US";

    public PD() {}


    public PD(DBPlayer player, Raw doc) {
        this.player = player;
        rank = doc.getRank(RankType.normal);
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
        if (afk) {
            player.name = player.name + "[gray]<AFK>[]";
        } else if (dRank != null && dRank.displayed) {
            player.name = player.name + dRank.suffix();
        } else if (sRank != null && sRank.displayed) {
            player.name = player.name + sRank.suffix();
        } else if (rank != null){
            player.name = player.name + rank.suffix();
        }
        if (rank != null) {
            player.admin = rank.admin;
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
