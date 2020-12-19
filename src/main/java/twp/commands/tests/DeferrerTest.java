package twp.commands.tests;

import arc.*;
import mindustry.game.*;
import twp.commands.*;
import twp.commands.Command.*;

public class DeferrerTest extends Test {
    public static void main(String[] args){
        init();
        
        Deferrer.terminal.run("", "exit");
        Deferrer.terminal.assertResult(Result.success);

        Deferrer.terminal.run("", "recover");
        Deferrer.terminal.assertResult(Result.recoverSuccess);
    }
}
