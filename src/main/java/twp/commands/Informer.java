package main.java.twp.commands;

import main.java.twp.database.Account;
import main.java.twp.database.enums.RankType;

import java.util.ArrayList;

import static main.java.twp.Main.db;

public class Informer extends Command {

    Informer() {
        name = "info";
        argStruct = "<id> [stats]";
        description = "Shows information about player.";
    }

    @Override
    public void run(String id, String... args) {
        Account ac = db.findAccount(args[0]);
        if (ac == null) {
            result = Result.notFound;
            return;
        }

        if (args.length == 1) {
            setArg(ac.basicStats());
            result = Result.info;
        } else {
            setArg(ac.stats());
            result = Result.stats;
        }
    }

    public static Informer general = new Informer();
}
