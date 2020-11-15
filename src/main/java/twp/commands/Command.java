package twp.commands;

import arc.func.Cons;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import twp.Global;
import twp.database.PD;
import twp.tools.Testing;

import static twp.Main.bundle;
import static twp.Main.db;

public abstract class Command {
    // constant
    public String name = "noname", argStruct, description = "description missing";

    // dynamic
    public Result result;
    public Object[] arg = {};
    public PD caller;

    abstract void run(String[] args, String id);

    void resolveArgs() {
        switch (result) {
            case playerNotFound:
                arg = new Object[] {
                    listPlayers()
                };
        }
    }

    public void registerGm(CommandHandler handler, PlayerCommandRunner runner) {
        CommandHandler.CommandRunner<Player> run = (args, player) -> {
            PD pd = db.online.get(player.uuid());

            if(pd == null) {
                Testing.Log("null PD when executing " + name + " command.");
                player.sendMessage("[yellow]Sorry there is an problem with your profile, " +
                        "try reconnecting or contact admins that you see this message.");
                return;
            }

            caller = pd;

            run(args, player.uuid());
            resolveArgs();

            if (runner != null) {
                runner.run(this, pd);
            } else {
                notifyCaller();
            }

        };

        if (argStruct == null) {
            handler.register(name, description, run);
        } else  {
            handler.register(name, argStruct, description, run);
        }
    }

    boolean checkArgCount(int count, int supposed) {
        if (count < supposed) {
            arg = new Object[] {count, supposed};
            result = Result.notEnoughArgs;
            return true;
        }
        return false;
    }

    boolean isNotInteger(String[] args, int idx) {
        if(Strings.canParsePositiveInt(args[idx])) {
            return false;
        }
        arg = new Object[] {idx + 1, args[idx]};
        result = Result.notInteger;
        return true;
    }

    public void registerCmp(CommandHandler handler, TerminalCommandRunner runner) {
        Cons<String[]> func = (args) -> {
            run(args, "");
            resolveArgs();

            if (runner != null) {
                runner.run(this);
            } else {
                notifyCaller();
            }
        };

        if (argStruct == null) {
            handler.register(name, description, func);
        } else  {
            handler.register(name, argStruct, description, func);
        }
    }

    public String getMessage() {
        return (result.general ? "" : (name + "-")) + result.name();
    }

    public void notifyCaller() {
        if(caller == null) {
            Log.info(Global.cleanColors(String.format(bundle.getDefault(getMessage()), arg)));
            return;
        }
        caller.sendServerMessage(getMessage(), arg);
    }

    public void kickCaller(int duration) {
        caller.kick(getMessage(), duration, arg);
    }

    public String listPlayers() {
        StringBuilder sb = new StringBuilder();
        for( PD pd : db.online.values()) {
            sb.append(pd.summarize()).append("\n");
        }
        return sb.substring(0, sb.length() -1);
    }

    void assertResult(Result supposed) {
        if(supposed != result) {
            throw new RuntimeException(supposed.name() + "!=" + result.name());
        }
    }

    public interface PlayerCommandRunner {
        void run(Command c, PD pd);
    }

    public interface TerminalCommandRunner {
        void run(Command c);
    }

    public enum Result {
        success,
        notFound,
        notExplicit,

        noPerm,
        wrongRank,
        wrongAccess,
        unprotectSuccess,
        alreadyProtected,
        confirm,
        confirmFail,
        confirmSuccess,
        invalidRequest,
        loginSuccess,
        notInteger(true),
        playerNotFound(true),
        notEnoughArgs(true),
        incorrectPassword(true);

        boolean general;

        Result() {}

        Result(boolean general) {
            this.general = general;
        }

    }
}
