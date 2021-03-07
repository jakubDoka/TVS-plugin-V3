package main.java.twp.discord;

import arc.util.Log;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import main.java.twp.Global;
import main.java.twp.tools.Json;
import main.java.twp.tools.Logging;

import java.util.HashMap;
import java.util.Optional;

import static java.lang.System.out;

public class Bot {
    public Config cfg;
    public Logger log;
    public DiscordApi api;
    public Long serverID;
    public Handler handler;

    public static String
            configDir = Global.config_dir + "/bot",
            configFile = configDir + "/config.json";

    private final HashMap<String, ServerTextChannel> channels = new HashMap<>();
    private final HashMap<String, Role> roles = new HashMap<>();

    public Bot(boolean initialized) {
        this.cfg = Json.loadJackson(configFile, Config.class);
        if(cfg == null) {
            return;
        }

        try {
            api = new DiscordApiBuilder().setToken(cfg.token).login().join();
        } catch (Exception ex){
            Logging.info("discord-failed");
            return;
        }

        for (Object o : cfg.channels.keySet()) {
            String key = (String) o;
            Optional<ServerTextChannel> optional = api.getServerTextChannelById(cfg.channels.get(key));
            if (!optional.isPresent()) {
                Logging.info("discord-channelNotFound", key);
                continue;
            }
            channels.put(key, optional.get());

            if(serverID == null) serverID = optional.get().getServer().getId();
        }

        for (Object o : cfg.roles.keySet()) {
            String key = (String) o;
            Optional<Role> optional = api.getRoleById(cfg.roles.get(key));
            if (!optional.isPresent()) {
                out.println(key + " role not found.");
                continue;
            }
            this.roles.put(key, optional.get());

            if(serverID == null) serverID = optional.get().getServer().getId();
        }

        log = new Logger(initialized);
        api.addMessageCreateListener(log);

        handler = new Handler(this, new CommandLoader());
        api.addMessageCreateListener(handler);
    }

    public ServerTextChannel Channel(Channels ch) {
        return channels.get(ch.name());
    }

    enum Channels {
        liveChat,
        commandLog,
        commands,
        rankLog,
    }
}
