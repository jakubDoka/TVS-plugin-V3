package twp.commands;

import twp.database.Account;
import twp.database.enums.RankType;
import twp.tools.Enums;

import static twp.Main.db;

// Command used for editing database
public class DBSetter extends Command {

    public DBSetter() {
        name = "dbset";
        argStruct = "<id> <field> <value/null>";
        description = "Directly edits player accounts. For example bring down a stats of farmers.";
    }

    String[] forbidden = {
            "name",
            "uuid",
            "ip",
            "password",
            "_id",
    };

    @Override
    public void run(String id, String... args) {
        if(!verifier.verify(id)) {
            result = Result.noPerm;
            return;
        }

        boolean forbid = false;
        for(String s : forbidden) {
            if(s.equals(args[1])) {
                forbid = true;
                break;
            }
        }

        if(forbid && !freeAccess) { // removing some of properties can break staff
            result = Result.wrongAccess;
            return;
        }

        if(Enums.contains(RankType.class, args[1])) { // we already have command for this
            result = Result.wrongCommand;
            return;
        }

        if(isNotInteger(args, 0)) {
            return;
        }

        long i = Long.parseLong(args[0]);

        Account doc = db.handler.getAccount(i);
        if( doc == null) {
            result = Result.notFound;
            return;
        }

        Object field = db.handler.get(i, args[1]);

        if(field == null) {
            setArg(1, doc.fieldList());
            result = Result.wrongOption; // ok
            return;
        }

        if(args[2].equals("null")) {
            db.handler.unset(i, args[1]);
            result = Result.unsetSuccess;
            return;
        }

        if (field instanceof String) {
            db.handler.set(i, args[1], args[2]);
        } else if (field instanceof Long) {
            if(isNotInteger(args, 2)) {
                return;
            }
            db.handler.set(i, args[1], Long.parseLong(args[2]));
        } else {
            result = Result.wrongAccess;
            return;
        }

        setArg(field.toString(), args[2]);

    }

    public static DBSetter
            terminal = new DBSetter(),
            game = new DBSetter() {{verifier = this::isPlayerAdmin;}};
}
