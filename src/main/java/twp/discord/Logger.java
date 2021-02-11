package twp.discord;

import mindustry.game.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.*;
import org.javacord.api.event.message.*;
import org.javacord.api.listener.message.*;
import twp.database.*;
import twp.database.enums.*;
import twp.tools.*;

import static twp.Main.*;
import static twp.discord.Bot.Channels.*;

public class Logger implements MessageCreateListener {
    public Logger(boolean initialized){
        if(initialized) return;
        Logging.on(EventType.PlayerChatEvent.class, e -> {
            if(bot == null) return;
            TextChannel chn = bot.Channel(commandLog);
            if(chn == null) return;
            if(e.message.startsWith("/") && !e.message.startsWith("/account")){
                PD pd = db.online.get(e.player.uuid());
                chn.sendMessage(Logging.translate("discord-commandLog", pd.player.name, pd.id, pd.rank.name, e.message));
            }
        });

        Logging.on(EventType.PlayerChatEvent.class, e -> {
            if(bot == null) return;
            TextChannel chn = bot.Channel(liveChat);
            if(chn == null) return;
            if(!e.message.startsWith("/")){
                PD pd = db.online.get(e.player.uuid());
                chn.sendMessage(Logging.translate("discord-serverMessage", pd.player.name, pd.id, e.message));
            }
        });
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
