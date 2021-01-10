package twp.discord;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import twp.commands.RankSetter;

import java.awt.*;

public class CommandLoader implements Handler.Loader {
    @Override
    public void load(Handler h) {
        h.addCommand(new Handler.Command("none") {
            {
                name = "help";
                purpose = "shows what you see right now";
            }
            @Override
            public void run(Handler.Context ctx) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("COMMANDS")
                        .setColor(Color.orange);

                StringBuilder sb = new StringBuilder()
                        .append("*!commandName - restriction - <necessary> [optional] {attachment} - description*\n");

                for (Handler.Command c : h.commands.values()) {
                    sb.append(c.getInfo()).append("\n");
                }

                ctx.channel.sendMessage(eb.setDescription(sb.toString()));
            }
        });

        RankSetter.terminal.registerDs(h);
    }
}
