package main.java.twp.discord;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

import static main.java.twp.discord.Bot.Channels.*;

public class Config {
    public String prefix = "!";
    public String token = "put it there neeeeebaaaaa";

    public HashMap<String, String>
            roles = new HashMap<>(),
            channels = new HashMap<String, String>(){{
                put(liveChat.name(), "");
                put(commandLog.name(), "");
                put(commands.name(), "");
            }};

    public HashMap<String, String[]> permissions = new HashMap<String, String[]>(){{
        put("setrank", new String[]{"admin role here", "other roles..."});
    }};

    public Config() {}

    @JsonCreator
    public Config(
            @JsonProperty("prefix") String prefix,
            @JsonProperty("token") String token,
            @JsonProperty("roles") HashMap<String, String> roles,
            @JsonProperty("channels") HashMap<String, String> channels,
            @JsonProperty("permissions") HashMap<String, String[]> permissions
    ) {
        if (prefix != null) {
            this.prefix = prefix;
        }

        if (token != null) {
            this.token = token;
        }


        if (roles != null) {
            this.roles = roles;
        }

        if (channels != null) {
            this.channels = channels;
        }

        if (roles != null) {
            this.permissions = permissions;
        }
    }
}
