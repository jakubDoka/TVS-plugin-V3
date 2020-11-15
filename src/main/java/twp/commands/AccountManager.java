package twp.commands;

import arc.util.Strings;
import twp.database.PD;
import twp.database.Raw;
import twp.tools.Security;
import twp.tools.Testing;

import java.util.HashMap;

import static twp.Main.db;
import static twp.Main.ranks;

public class AccountManager extends Command {
    static HashMap<Long, String> confirms = new HashMap<>();

    public AccountManager() {
        name = "account";
        argStruct = "<abandon/new/id> [password]";
        description = "For account management, you can create new account of protect current one with password";
    }

    @Override
    public void run(String[] args, String id) {
        PD pd = db.online.get(id);

        if(pd.rank == ranks.griefer) {
            arg = new Object[] {ranks.griefer.getSuffix()};
            result = Result.noPerm;
            return;
        }

        switch (args[0]) {
            case "unprotect":
                if (checkArgCount(args.length, 2)) {
                    return;
                }
                Raw raw1 = pd.getDoc();
                if (raw1.isProtected()) {
                    if (raw1.getPassword().equals(Security.hash2(args[1]))) {
                        db.handler.remove(pd.id, "password");
                        result = Result.unprotectSuccess;
                    } else {
                        result = Result.incorrectPassword;
                    }
                    return;
                }
            case "protect":
                if (checkArgCount(args.length, 2)) {
                    return;
                }
                if (pd.getDoc().isProtected()) {
                    result = Result.alreadyProtected;
                    return;
                }
                String pa = confirms.get(pd.id);
                if(pa != null) {
                    if (pa.equals(args[1])) {
                        db.handler.set(pd.id, "password", Security.hash2(args[1]));
                        result = Result.confirmSuccess;
                    } else {
                        result = Result.confirmFail;
                    }
                    confirms.remove(pd.id);
                    return;
                }
                confirms.put(pd.id, args[1]);
                result = Result.confirm;
                return;
            case "abandon":
            case "new":
                if(!args[0].equals("abandon") && (!pd.paralyzed && !pd.getDoc().isProtected())){
                    result = Result.notExplicit;
                    return;
                }
                db.handler.makeNewAccount(pd.player.uuid, pd.player.ip);
                db.disconnectAccount(pd);
                result = Result.success;
                return;
            default:
                if(isNotInteger(args, 0)) {
                    return;
                }
                long id1 = Long.parseLong(args[0]);
                Raw raw = db.handler.getDoc(id1);
                if(raw == null) {
                    result = Result.notFound;
                    return;
                }

                String pass = args.length == 2 ? args[1] : "";

                Object password = raw.getPassword();
                if(!pd.player.ip.equals(raw.getIp()) && !pd.player.uuid.equals(raw.getUuid()) && password == null) {
                    result = Result.invalidRequest;
                    return;
                } else if(password == null || password.equals(Security.hash(pass)) || password.equals(Security.hash2(pass))){
                    db.disconnectAccount(pd);
                    db.handler.setIp(id1, pd.player.ip);
                    db.handler.setUuid(id1, pd.player.uuid);
                    result = Result.loginSuccess;
                } else {
                    result = Result.incorrectPassword;
                }
        }
    }

    public static AccountManager game = new AccountManager();
}
