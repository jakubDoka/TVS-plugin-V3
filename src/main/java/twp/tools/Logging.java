package twp.tools;

import arc.*;
import arc.func.*;
import arc.util.*;
import org.junit.platform.commons.util.*;
import twp.*;
import twp.database.*;

import java.io.*;
import java.sql.*;
import java.text.*;

import static twp.Main.*;

// Serializing errors and sending messages to cmd is performed from here
public class Logging {
    static final String outDir = Global.dir + "/errors/";

    public static void main(String[] args){
        log("hello");
        log("hello");
    }

    public static void info(String key, Object ...args) {
        Log.info(Text.cleanColors(Text.format(bundle.getDefault(key), args)));
    }

    public static void log(String message) {
        log(new RuntimeException(message));
    }

    public static void sendMessage(String key, Object ...args) {
        db.online.forEachValue(pd -> {
            pd.next().sendServerMessage(key, args);
        });
    }

    public static void log(Throwable t) {
        String ex = ExceptionUtils.readStackTrace(t);
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss-SSS z");
        Date date = new Date(System.currentTimeMillis());
        File f = new File(outDir+formatter.format(date));

        try {
            Json.makeFullPath(f.getAbsolutePath());
            f.createNewFile();
            PrintWriter out = new PrintWriter(f.getAbsolutePath());

            out.println(ex);
            out.close();
        } catch(IOException e) { e.printStackTrace();}
    }

    public static <T> void on(Class<T> event, Cons<T> cons) {
        Events.on(event, e -> {
            try{
                cons.get(e);
            } catch(Exception ex) {
                log(ex);
            }
        });
    }

    public static <T> void run(Object event, Runnable listener) {
        Events.run(event, () -> {
            try{
                listener.run();
            } catch(Exception ex) {
                log(ex);
            }
        });
    }
}
