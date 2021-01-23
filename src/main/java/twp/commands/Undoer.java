package twp.commands;

import twp.database.Account;
import twp.security.Action;

import static twp.Main.db;

public class Undoer extends Command {
    public Undoer() {
        name = "undo";
        argStruct = "<amount> [id/name]";
        description = "undo undoes the action of targeted player, if you do not provide player your actions are undone";
    }

    @Override
    public void run(String id, String... args) {
        Account data = (args.length == 2 || caller == null) ? db.findAccount(args[1]) : caller.getAccount();
        if (data == null) {
            playerNotFound();
            return;
        }

        if (!verifier.verify(id) && data.getId() != caller.id) {
            result = Result.noPerm;
            return;
        }

        if(isNotInteger(args, 0)) {
            return;
        }

        Action.execute(data.getId(), Integer.parseInt(args[0]));
    }

    public static Undoer game = new Undoer(){{verifier = this::isPlayerAdmin;}};
}
