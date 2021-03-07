package main.java.twp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import main.java.twp.tools.Json;

import java.util.*;

// plugin config is stored here
public class Global {
    public static final String dir = "config/mods/worse";
    public static final String config_dir = dir + "/config";
    public static final String save_dir = config_dir + "/saves";
    public static final String config_file = config_dir + "/config.json";
    public static final String weapons = config_dir + "/weapons.json";
    public static final String weaponLinking = config_dir + "/weapon_linking.json";

    public static Config loadConfig() {
        Config config = Json.loadJackson(config_file, Config.class);
        if (config == null) return new Config();
        return config;
    }

    public static class Config {
        public String salt = "TWS";

        public HashMap<String, String > rules;
        public HashMap<String, String > guide;
        public HashMap<String, String > welcomeMessage;

        public int consideredPassive = 10; // after how many missed votes is player considered passive
        public int shipLimit = 3; // maximum amount of ships players acn have
        public long doubleClickSpacing = 300; // double click sensitivity
        public int maxNameLength = 25; // if name is longer then this amount it is truncated
        public long testPenalty = 15 * 60 * 1000; // how often can players take test


        public Security sec = new Security();
        public VPN vpn = new VPN();
        public Database db = new Database();
        public Loadout loadout = new Loadout();

        public Config() {}
    }

    public static class Database {
        public String name = "mindustryServer"; // database name
        public String players = "PlayerData"; // player collection name
        public String maps = "MapData"; // map collection name
        public String address = "mongodb://127.0.0.1:27017"; // database host

    }

    public static class Loadout {
        public String name = "Loadout"; // loadout collection
        public int shipTravelTime = 60 * 3;
        public int shipCapacity = 3000;
    }

    public static class Security {
        public int actionLimit = 50; // how many actions triggers protection
        public long actionLimitFrame = 1000 * 2; // how frequently is action counter reset
        public long actionUndoTime = 1000 * 10; // how old actions will be reverted after protection trigger
        public int actionMemorySize = 5; // how many actions will be saved in tile for inspection
    }

    public static class VPN {
        public String api;
        public int timeout;
    }
}
