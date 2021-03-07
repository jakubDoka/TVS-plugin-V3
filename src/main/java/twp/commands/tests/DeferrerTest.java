package main.java.twp.commands.tests;

import main.java.twp.commands.*;
import main.java.twp.commands.Command.*;

public class DeferrerTest extends Test {
    public static void main(String[] args){
        init();
        
        Deferrer.terminal.run("", "exit");
        Deferrer.terminal.assertResult(Result.success);

        Deferrer.terminal.run("", "recover");
        Deferrer.terminal.assertResult(Result.recoverSuccess);
    }
}
