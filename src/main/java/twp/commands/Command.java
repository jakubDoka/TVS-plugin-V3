package twp.commands;

import arc.func.Cons;
import arc.util.CommandHandler;
import arc.util.Strings;
import org.junit.platform.commons.util.ExceptionUtils;
import twp.database.PD;
import mindustry.gen.Player;
import twp.discord.Handler;
import twp.tools.Logging;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


import static twp.Main.*;

// Command is base class of any command and contains utility for making commands bit more cleaner and organised
// One good advice, dont write your game in java... newer.
public abstract class Command {
    public boolean freeAccess;
    Verifier verifier = (id) -> true;
    // constant
    public String name = "noname", argStruct, description = "description missing";

    // dynamic
    public Result result = Result.success;
    public Object[] arg = {};
    public PD caller;

    // main behavior
    public abstract void run(String id, String ...args);

    void setArg(Object ... values) {
        arg = values;
    }

    // Shorthand for checking whether correct amount of arguments were provided
    boolean checkArgCount(int count, int supposed) {
        if (count < supposed) {
            setArg(count, supposed);
            result = Result.notEnoughArgs;
            return true;
        }
        return false;
    }

    // Shorthand for checking and handling invalid non integer arguments
    boolean isNotInteger(String[] args, int idx) {
        if(Strings.canParsePositiveInt(args[idx])) {
            return false;
        }
        setArg(idx + 1, args[idx]);
        result = Result.notInteger;
        return true;
    }

    // for registration of commandline commands
    public void registerTm(CommandHandler handler, TerminalCommandRunner runner) {
        freeAccess = true;
        Cons<String[]> func = (args) -> {
            try {
                result = Result.success;
                run("", args);
                if (runner != null) {
                    runner.run(this);
                } else {
                    notifyCaller();
                }
            } catch (Exception ex) {
                result = Result.bug;
                try {
                    notifyCaller();
                } catch (Exception e) {
                    Logging.log(ex);
                }
                Logging.log(ex);
            }
        };

        if (argStruct == null) {
            handler.register(name, description, func);
        } else  {
            handler.register(name, argStruct, description, func);
        }
    }

    // For registration of in-game commands
    public void registerGm(CommandHandler handler, PlayerCommandRunner runner) {
        CommandHandler.CommandRunner<Player> run = (args, player) -> {
            PD pd = db.online.get(player.uuid());

            if(pd == null) {
                Logging.log("null PD when executing " + name + " command.");
                player.sendMessage("[yellow]Sorry there is an problem with your profile, " +
                        "try reconnecting or contact admins that you see this message.");
                return;
            }

            caller = pd;

            try {
                result = Result.success;
                run(player.uuid(), args);
                if (runner != null) {
                    runner.run(this, pd);
                } else {
                    notifyCaller();
                }
            } catch (Exception ex) {
                result = Result.bug;
                // fucking java and deadlocks
                try {
                    notifyCaller();
                } catch (Exception e) {
                    Logging.log(e);
                }
                Logging.log(ex);
            }
        };

        if (argStruct == null) {
            handler.register(name, description, run);
        } else  {
            handler.register(name, argStruct, description, run);
        }
    }

    public void registerDs(Handler handler, DiscordCommandRunner run) {
        handler.addCommand(new Handler.Command(argStruct) {
            {
                name = Command.this.name;
                purpose = description;
            }
            @Override
            public void run(Handler.Context ctx) {
                queue.post(() -> {
                    try {
                        result = Result.success;
                        Command.this.run("", ctx.args);
                        if (run != null) {
                            run.run(ctx, Command.this);
                        } else {
                            ctx.reply(getMessage(), Command.this.arg);
                        }
                    } catch (Exception e) {
                        ctx.reply("discord-internalError", ExceptionUtils.readStackTrace(e));
                    }
                });
            }
        });
    }

    // getMessage returns bundle key based of result
    public String getMessage() {
        return getMessage(result);
    }

    // getMessage returns bundle key based of result
    public String getMessage(Result r) {
        return (r.general ? "" : (name + "-")) + r.name();
    }

    // notifyCaller sends message to caller, its just a shorthand and is atomaticly called if
    // command lambda is null
    public void notifyCaller() {
        if(result == Result.none) {
            return;
        }

        if(caller == null) {
            Logging.info(getMessage(), arg);
            return;
        }
        caller.sendServerMessage(getMessage(), arg);
    }

    // shorthand for kicking caller
    public void kickCaller(int duration) {
        caller.kick(getMessage(), duration, arg);
    }

    // creates string of information about online players
    public String listPlayers() {
        StringBuilder sb = new StringBuilder();

        for(PD pd : db.online.values()) {
            sb.append(pd.summarize()).append("\n");
        }

        return sb.substring(0, sb.length() -1);
    }

    boolean isPlayerAdmin(String id) {
        PD pd = db.online.get(id);
        return pd != null && pd.rank.admin;
    }

    void playerNotFound() {
        result = Result.playerNotFound;
        setArg(listPlayers());
    }

    boolean cannotInteract(String id) {
        if(db.online.get(id).cannotInteract()) {
            result = Result.noPerm;
            return true;
        }
        return false;
    }

    public boolean wrongOption(int idx, String[] args, String options) {
        return wrongOption(idx, args, Arrays.asList(options.split(" ")), null);
    }

    public boolean wrongOption(int idx, String[] args, List<String> options) {
        return wrongOption(idx, args, options, null);
    }

    public boolean wrongOption(int idx, String[] args, List<String> options, String raw) {
        if(options == null) {
            result = Result.wrongOption;
            setArg(idx, raw);
            return false;
        } else if(!options.contains(args[idx])) {
            result = Result.wrongOption;
            if(raw == null) {
                StringBuilder sb = new StringBuilder();
                options.forEach(e -> sb.append(e).append(" "));
                raw = sb.toString();
            }
            setArg(idx, raw);
            return true;
        }

        return false;
    }

    // Used for testing commands
    public void assertResult(Result supposed) {
        try{
            if (result != Result.none){
                Logging.info(getMessage(), arg);
            }

            if(supposed != result){
                throw new RuntimeException(supposed.name() + "!=" + result.name());
            }
        } finally {
            result = Result.success;
        }
    }

    // lambda for commands invoiced by players
    public interface PlayerCommandRunner {
        void run(Command c, PD pd);
    }

    // lambda for commands called from command prompt
    public interface TerminalCommandRunner {
        void run(Command c);
    }

    public interface DiscordCommandRunner {
        void run(Handler.Context ctx, Command self);
    }

    public interface Verifier {
        boolean verify(String id);
    }

    // Result enum contains all possible results command can return
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
        unsetSuccess,
        emptySlice,
        invalidSlice,
        noOneOnline,
        successOnline,
        wrongCommand,
        redundant,
        cannotApplyToSelf,
        notExist,
        invalidFile,
        enableFail,
        addSuccess,
        alreadyEnabled,
        alreadyDisabled,
        disableFail,
        updateSuccess,
        updateFail,
        alreadyAdded,
        recoverSuccess,
        penalty,
        start,
        hint,
        testFail,

        bug(true),
        notInteger(true),
        playerNotFound(true),
        notEnoughArgs(true),
        incorrectPassword(true),
        alreadyVoting(true),
        cannotVote(true),
        voteStartSuccess(true),
        invalidVoteSession(true),
        voteSuccess(true),
        alreadyVoted(true),
        wrongOption(true),

        none, rateSuccess, info, stats, fail, invalidSearch;

        boolean general;

        Result() {}

        Result(boolean general) {
            this.general = general;
        }
    }
}
