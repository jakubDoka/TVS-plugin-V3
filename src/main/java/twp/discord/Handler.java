package main.java.twp.discord;

import arc.util.Log;
import org.apache.commons.codec.binary.StringUtils;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.jsoup.internal.StringUtil;
import main.java.twp.tools.Logging;

import java.util.*;

import static main.java.twp.Main.bot;

public class Handler implements MessageCreateListener {
    ServerTextChannel com;
    TextChannel cur;
    HashMap<String, Command> commands = new HashMap<>();

    public Handler(Bot bot, Loader... loader) {
        for(Loader l : loader) {
            l.load(this);
        }

        Optional<Server> server = bot.api.getServerById(bot.serverID);
        if (!server.isPresent()) {
            return;
        }

        for(String c : bot.cfg.permissions.keySet()) {
            Command cm = commands.get(c);
            if(cm == null) {
                continue;
            }

            String[] perms = bot.cfg.permissions.get(c);
            for(String p : perms) {
                Optional<Role> role = server.get().getRoleById(p);
                if (role.isPresent()) {
                    cm.restriction.add(role.get());
                } else {
                    Logging.info("discord-roleNotFound", cm.name, p);
                }
            }

        }
    }

    public void addCommand(Command c) {
        commands.put(c.name, c);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser() || !event.getMessageContent().startsWith(bot.cfg.prefix)) {
            return;
        }

        com = bot.Channel(Bot.Channels.commands);
        cur = event.getChannel();

        if(com.getId() != cur.getId()) {
            Logging.sendDiscordMessage(cur ,"discord-goToCommands", com.getMentionTag());
            return;
        }

        try {
            Context ctx = new Context(event, this);
            if (!ctx.command.can(ctx)) {
                ctx.reply("discord-noPermission", ctx.command.listRestrictions());
                return;
            }
            ctx.command.run(ctx);
        } catch (NoSuchCommandException e) {
            Logging.sendDiscordMessage(cur, "discord-noSuchCommand", bot.cfg.prefix);
        } catch (WrongArgAmountException e) {
            Logging.sendDiscordMessage(cur, "discord-tooFewArguments", bot.cfg.prefix);
        } catch (NoSuchElementException e) {
            Logging.sendDiscordMessage(cur, "discord-strangeNoUser");
        } catch (Exception e) {
            Logging.log(e);
        }
    }


    public static abstract class Command {
        int minArg, maxArg, minAttachment;
        public String name, args, purpose;
        ArrayList<Role> restriction = new ArrayList<>();

        public Command(String args) {
            this.args = args;

            for(int i = 0; i < args.length(); i++) {
                switch (args.charAt(i)) {
                    case '<':
                        minArg++;
                        maxArg++;
                        break;
                    case '{':
                        minAttachment++;
                        break;
                    case '[':
                        maxArg++;
                }
            }
        }

        boolean can(Context ctx) throws NoSuchElementException {
            if (ctx.event.isPrivateMessage()) return false;
            if (restriction.size() == 0) return true;
            List<Role> roles = ctx.event.getMessageAuthor().asUser().get().getRoles(ctx.event.getServer().get());
            for(Role r : roles){
                for(Role i : restriction) {
                    if(r.getId() == i.getId()) return true;
                }
            }
            return false;
        }

        String listRestrictions() {
            StringBuilder sb = new StringBuilder();
            for(Role r : restriction) {
                sb.append("**").append(r.getName()).append("**");
            }
            return sb.toString();
        }

        public abstract void run(Context ctx);

        protected String getInfo() {
            return String.format("%s**%s**-*%s*-%s", bot.cfg.prefix, name, args, purpose);
        }
    }

    public static class Context {
        public String name, content;
        public String[] args = {};
        public MessageCreateEvent event;
        public User user;
        public TextChannel channel;
        public Command command;

        Context(MessageCreateEvent event, Handler handler) throws Exception {
            this.event = event;
            this.content = event.getMessageContent();
            this.channel = event.getChannel();

            String[] all = content.split(" ", 2);
            name = all[0].replace(bot.cfg.prefix, "");

            command = handler.commands.get(name);
            if (command == null) {
                throw new NoSuchCommandException();
            }

            if(all.length == 2) {
                args = all[1].split(" ", command.maxArg);
            }

            if(args.length < command.minArg) {
                throw new WrongArgAmountException();
            }

            user = event.getMessageAuthor().asUser().get();
        }

        public void reply(String key, Object ...args) {
            Logging.sendDiscordMessage(channel, key, args);
        }
    }

    static class WrongArgAmountException extends Exception { }
    static class NoSuchCommandException extends Exception { }

    interface Loader {
        void load(Handler h);
    }
}
