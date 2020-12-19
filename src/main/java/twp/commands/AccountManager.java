package twp.commands;

import twp.database.PD;
import twp.database.Account;
import twp.tools.Security;

import java.util.HashMap;

import static twp.Main.db;
import static twp.Main.ranks;

// AccountManager lets players to manage their account
// create new, abandon it and protect it.
public class AccountManager extends Command {
    static HashMap<Long, String> confirms = new HashMap<>();

    public AccountManager() {
        name = "account";
        argStruct = "<abandon/new/id> [password]";
        description = "For account management, you can create new account of protect current one with password";
    }

    @Override
    public void run(String id, String ...args) {
        PD pd = db.online.get(id);

        // griefers cannot manipulate with their account, griefer rank would be then worthless
        if(pd.rank == ranks.griefer) {
            setArg(ranks.griefer.getSuffix());
            result = Result.noPerm;
            return;
        }

        switch (args[0]) {
            case "unprotect":
                if (checkArgCount(args.length, 2)) {
                    return;
                }
                Account account1 = pd.getAccount();
                if (account1.isProtected()) {
                    if (account1.getPassword().equals(Security.hash2(args[1]))) {
                        db.handler.unset(pd.id, "password");
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
                if (pd.getAccount().isProtected()) {
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
                // this is little protection for dummies, we dont need accidents, nor we need dead data in database
                if(!args[0].equals("abandon") && (!pd.paralyzed && !pd.getAccount().isProtected())){
                    result = Result.notExplicit;
                    return;
                }
                db.handler.makeNewAccount(pd.player.uuid, pd.player.ip);
                db.disconnectAccount(pd);
                return;
            default:
                if(isNotInteger(args, 0)) {
                    return;
                }

                long id1 = Long.parseLong(args[0]);
                Account account = db.handler.getAccount(id1);
                if(account == null) {
                    result = Result.notFound;
                    return;
                }

                String pass = args.length == 2 ? args[1] : "";

                Object password = account.getPassword();
                if(!pd.player.ip.equals(account.getIp()) && !pd.player.uuid.equals(account.getUuid()) && password == null) {
                    result = Result.invalidRequest;
                } else if(password == null || password.equals(Security.hash(pass)) || password.equals(Security.hash2(pass))){ // due to backward compatibility there are two hashes
                    db.disconnectAccount(pd);
                    db.handler.setIp(id1, pd.player.ip);
                    db.handler.setUuid(id1, pd.player.uuid);
                    result = Result.loginSuccess;
                } else {
                    result = Result.incorrectPassword;
                }
        }
    }

    public static AccountManager
            game = new AccountManager();
}
