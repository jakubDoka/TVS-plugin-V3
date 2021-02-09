package twp.discord;

import arc.util.Log;
import mindustry.game.EventType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import twp.database.*;
import twp.database.enums.*;
import twp.tools.Logging;

import static twp.Main.bot;
import static twp.Main.db;
import static twp.discord.Bot.Channels.*;

public class Logger implements MessageCreateListener {
    public Logger(Bot bot) {
        TextChannel chn1 = bot.Channel(commandLog);
        if (chn1 != null) {
            Logging.on(EventType.PlayerChatEvent.class, e -> {
                if (e.message.startsWith("/") && !e.message.startsWith("/account")) {
                    PD pd = db.online.get(e.player.uuid());
                    chn1.sendMessage(Logging.translate("discord-commandLog", pd.player.name, pd.id, pd.rank.name, e.message));
                }
            });
        }

        TextChannel chn2 = bot.Channel(liveChat);
        if(chn2 != null) {
            Logging.on(EventType.PlayerChatEvent.class, e -> {
                if (!e.message.startsWith("/")) {
                    PD pd = db.online.get(e.player.uuid());
                    chn2.sendMessage(Logging.translate("discord-serverMessage",pd.player.name, pd.id, e.message));
                }
            });
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        MessageAuthor at = event.getMessageAuthor();
        if(at.isBotUser()) {
            return;
        }
        Channel curr = event.getChannel();
        TextChannel chn = bot.Channel(liveChat);
        if (chn != null && chn.getId() == curr.getId()) {
            for(PD pd : db.online.values()) {
                pd.sendDiscordMessage(event.getMessageContent(), at.getName());
            }
        }
    }

    public void logRankChange(long id, Rank rank, String comment) {
        TextChannel chn = bot.Channel(rankLog);
        Account ac = db.handler.getAccount(id);
        if (chn != null && ac != null){
            chn.sendMessage(String.format(
                "player:**%s**(id:%d)\nchange: %s -> %s\nreason: %s",
                ac.getName(),
                id,
                ac.getRank(RankType.rank).getSuffix(),
                rank.getSuffix(),
                comment
            ));
        }
    }
}
