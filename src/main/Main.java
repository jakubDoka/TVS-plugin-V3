package main;


import arc.util.CommandHandler;
import arc.util.Log;
import main.commands.AccountManager;
import main.commands.RankSetter;
import main.database.*;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

public class Main extends Plugin {
    public static Ranks ranks = new Ranks();
    public static Database db = new Database();


    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("setrank", "<name/id> <rank> ", "Sets players rank.", args -> {
            Log.info(RankSetter.terminal.run(args, ""));
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler) {
        handler.<Player>register("setrank", "<name/id> <rank> ", "Sets players rank.", (args, player) -> {
            player.sendMessage(RankSetter.game.run(args, player.uuid())
            );
        });
        handler.<Player>register("account", "<abandon/new/id> [password] ", "Sets players rank.", (args, player) -> {
            String result = AccountManager.game.run(args, player.uuid());
            if(result.equals(AccountManager.game.success) || result.equals(AccountManager.game.successLogin)) {
                player.con.kick("Command wos successful, you can join egan.", 0);
            } else {
                player.sendMessage(result);
            }
        });
    }
}
