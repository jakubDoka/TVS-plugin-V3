package twp.commands;

import arc.func.Cons;
import arc.util.CommandHandler;
import mindustry.gen.Player;
import twp.database.PD;

import static twp.Main.db;

public abstract class Command {
    Object[] args = new Object[0];
    public String name = "noname";
    public String argStruct;
    public String description = "description missing";
    public String
            noPerm = "-noPerm",
            success = "-success",
            notFound = "-notFound",
            notInteger = "-notInteger";

    public abstract String run(String[] args, String id);

    public void register(CommandHandler handler, PlayerCommandRunner runner) {
        CommandHandler.CommandRunner<Player> run = (args, player) -> {
            PD pd = db.online.get(player.uuid());
            if(pd == null) {
                new RuntimeException("null PD when executing " + name + " command.").printStackTrace();
                player.sendMessage("[yellow]Sorry there is an problem with your profile, " +
                        "try reconnecting or contact admins that you see this message.");
                return;
            }

            String result = run(args, player.uuid());

            runner.run(result, result.startsWith("-") ? result.substring(1) : (name + "-" + result), pd);
        };

        if (argStruct == null) {
            handler.register(name, description, run);
        } else  {
            handler.register(name, argStruct, description, run);
        }
    }

    public void register(CommandHandler handler, Cons<String[]> runner) {
        if (argStruct == null) {
            handler.register(name, description, runner);
        } else  {
            handler.register(name, argStruct, description, runner);
        }
    }

    public interface PlayerCommandRunner {
        void run(String result, String bundle, PD pd);
    }
}
