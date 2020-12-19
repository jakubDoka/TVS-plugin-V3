package twp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import twp.security.LockMap.*;
import twp.tools.Json;

import java.util.*;

public class Global {
    public static final String dir = "config/mods/worse";
    public static final String config_dir = dir + "/config";
    public static final String save_dir = config_dir + "/saves";
    public static final String config_file = config_dir + "/config.json";
    public static Config config = loadConfig();

    public static Config loadConfig() {
        Config config = Json.loadJackson(config_file, Config.class);
        if (config == null) return new Config();
        return config;
    }

    public static class Config {
        public String symbol = "[green]<Survival>[]";
        public String alertPrefix = "!!";
        public String dbName = "mindustryServer";
        public String playerCollection = "PlayerData";
        public String mapCollection = "MapData";
        public String dbAddress = "mongodb://127.0.0.1:27017";
        public String salt = "TWS";
        public HashMap<String, String > rules;
        public HashMap<String, String > guide;
        public HashMap<String, String > welcomeMessage;
        public int consideredPassive = 10;

        public int vpnTimeout;
        public String vpnApi;
        public int actionMemorySize = 5;
        public long doubleClickSpacing = 300;


        public Config() {}

        @JsonCreator
        public Config(
                @JsonProperty("mapCollection") String mapCollection,
                @JsonProperty("consideredPassive") int consideredPassive,
                @JsonProperty("symbol") String symbol,
                @JsonProperty("playerCollection") String playerCollection,
                @JsonProperty("dbAddress") String dbAddress,
                @JsonProperty("alertPrefix") String alertPrefix,
                @JsonProperty("dbName") String dbName,
                @JsonProperty("rules") HashMap<String, String> rules,
                @JsonProperty("guide") HashMap<String, String> guide,
                @JsonProperty("welcomeMessage") HashMap<String, String> welcomeMessage,
                @JsonProperty("vpnApi") String vpnApi,
                @JsonProperty("vpnTimeout") int vpnTimeout,
                @JsonProperty("actionMemorySize") int actionMemorySize,
                @JsonProperty("doubleClickSpacing") long doubleClickSpacing
        ){
            if(symbol != null) this.symbol = symbol;
            if(dbAddress != null) this.dbAddress = dbAddress;
            if(playerCollection != null) this.playerCollection = playerCollection;
            if(alertPrefix != null) this.alertPrefix = alertPrefix;
            if(dbName != null) this.dbName = dbName;
            if(mapCollection != null) this.mapCollection = mapCollection;
            if(actionMemorySize != 0) this.actionMemorySize = actionMemorySize;
            if(doubleClickSpacing != 0) this.doubleClickSpacing = doubleClickSpacing;

            this.consideredPassive = consideredPassive;
            this.rules = rules;
            this.guide = guide;
            this.welcomeMessage = welcomeMessage;
            this.vpnApi = vpnApi;
            this.vpnTimeout = vpnTimeout;
        }
    }
}
