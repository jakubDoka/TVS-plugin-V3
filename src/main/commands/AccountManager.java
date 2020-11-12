package main.commands;

import arc.util.Log;
import arc.util.Strings;
import main.database.PD;
import main.database.Raw;
import main.tools.Security;

import java.util.HashMap;

import static main.Main.db;
import static main.Main.ranks;

public class AccountManager {
    static HashMap<Long, String> confirms = new HashMap<>();

    public String
            noPerm = "noPerm",
            success = "success",
            notExplicit = "notExplicit",
            notInteger = "notInteger",
            notFound = "notFound",
            invalidRequest = "invalidRequest",
            incorrectPassword = "incorrectPassword",
            successLogin = "successLogin",
            notEnoughArgs = "notEnoughArgs",
            confirmSuccess = "confirmSuccess",
            confirmFail = "confirmFail",
            confirm = "confirm",
            unprotectSuccess = "unprotectSuccess",
            alreadyProtected = "alreadyProtected";

    public String run(String[] args, String id) {
        PD pd = db.online.get(id);
        if(pd == null) {
            new RuntimeException("players account is null ewen though he is present").printStackTrace();
            return "command failed";
        }

        if(pd.rank == ranks.griefer) {
            return noPerm;
        }
        switch (args[0]) {
            case "unprotect":
                if (args.length < 2) {
                    return notEnoughArgs;
                }
                Raw raw1 = pd.getDoc();
                if (raw1.isProtected()) {
                    if (raw1.getPassword().equals(Security.hash2(args[1]))) {
                        db.handler.remove(pd.id, "password");
                        return unprotectSuccess;
                    } else {
                        return incorrectPassword;
                    }
                }
            case "protect":
                if (args.length < 2) {
                    return notEnoughArgs;
                }
                if (pd.getDoc().isProtected()) {
                    return alreadyProtected;
                }
                String pa = confirms.get(pd.id);
                if(pa != null) {
                    if (pa.equals(args[1])) {
                        db.handler.set(pd.id, "password", Security.hash2(args[1]));
                        return confirmSuccess;
                    } else {
                        confirms.remove(pd.id);
                        return confirmFail;
                    }
                }
                confirms.put(pd.id, args[1]);
                return confirm;
            case "abandon":
            case "new":
                if(!args[0].equals("abandon") && (!pd.paralyzed && !pd.getDoc().isProtected())){
                    return notExplicit;
                }
                db.handler.makeNewAccount(pd.player.uuid, pd.player.ip);
                db.disconnectAccount(pd);
                return success;
            default:
                if(!Strings.canParsePositiveInt(args[0])) {
                    return notInteger;
                }
                long id1 = Long.parseLong(args[0]);
                Raw raw = db.handler.getDoc(id1);
                if(raw == null) {
                    return notFound;
                }

                String pass = args.length == 2 ? args[1] : "";

                Object password = raw.getPassword();
                if(!pd.player.ip.equals(raw.getIp()) && !pd.player.uuid.equals(raw.getUuid()) && password == null) {
                   return invalidRequest;
                } else if(password == null || password.equals(Security.hash(pass)) || password.equals(Security.hash2(pass))){
                    db.disconnectAccount(pd);
                    db.handler.setIp(id1, pd.player.ip);
                    db.handler.setUuid(id1, pd.player.uuid);
                    return successLogin;
                } else {
                    return incorrectPassword;
                }
        }
    }

    public static AccountManager game = new AccountManager();
}
