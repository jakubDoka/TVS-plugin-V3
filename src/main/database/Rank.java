package main.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static main.Main.db;
import static main.Main.ranks;


public class Rank implements Serializable {
    static final long hour = 1000 * 60 * 60;
    public String name = "noName";
    public String color = "red";
    public boolean displayed = true;
    public boolean admin = false;
    public int value=0;
    public HashSet<String> permissions = new HashSet<>();
    public HashSet<String> linked = null;
    public HashMap<String,String> description = null;
    public HashMap<String, HashMap<String,Integer>> quests = null;
    public ArrayList<String> pets = null;

    public Rank() {}

    @JsonCreator public Rank(
            @JsonProperty("displayed") boolean displayed,
            @JsonProperty("admin") boolean admin,
            @JsonProperty("name") String name,
            @JsonProperty("color") String color,
            @JsonProperty("description") HashMap<String,String> description,
            @JsonProperty("value") int value,
            @JsonProperty("permissions") HashSet<String> permissions,
            @JsonProperty("linked") HashSet<String> linked,
            @JsonProperty("quests") HashMap<String, HashMap<String,Integer>> quests,
            @JsonProperty("pets")ArrayList<String> pets){

        this.displayed = displayed;
        this.admin = admin;
        if(name != null) this.name = name;
        if(color != null) this.color = color;
        this.description = description;
        this.value = value;
        if(permissions != null) this.permissions = permissions;
        this.linked = linked;
        this.quests = quests;
        this.pets = pets;
    }

    @JsonIgnore public boolean isPermanent(){
        return quests == null && linked == null;
    }

    public boolean condition(Raw tested, PD pd){
        if (pd.hasObtained(this)) return true;
        if(linked != null) {
            for (String l : linked){
                Rank other = ranks.special.get(l);
                if (pd.hasObtained(other)) continue;
                if(!other.condition(tested, pd)) return false;
            }
        }
        if(quests == null){
            return linked != null;
        }
        for(String stat : quests.keySet()){
            HashMap<String,Integer> quest = quests.get(stat);
            Long val = tested.getStat(stat);
            long played = tested.getStat(Stat.playTime)/hour;
            if(played == 0 ){
                played = 1;
            }
            if(quest.containsKey(Mod.required.name()) && val < quest.get(Mod.required.name())) return false;
            if(quest.containsKey(Mod.frequency.name()) && quest.get(Mod.frequency.name()) > val/played) return false;
            if(quest.containsKey(Mod.best.name()) && db.handler.getPlace(tested, stat) > quest.get(Mod.best.name())) return false;
        }
        pd.addRank(this);
        return true;
    }


    @JsonIgnore public String getSuffix(){
        return "["+color+"]<"+name+">[]";
    }

    @JsonIgnore public String suffix() {
        return displayed ? getSuffix() : "";
    }

    enum Mod{
        best,
        required,
        frequency
    }
}
