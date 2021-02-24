package twp.tools;

import arc.*;
import arc.func.*;
import arc.util.*;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.junit.platform.commons.util.*;
import twp.*;
import twp.database.*;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.concurrent.CompletableFuture;

import static twp.Main.*;

// Serializing errors and sending messages to cmd is performed from here
public class Logging {
    static final String outDir = Global.dir + "/errors/";
    static SimpleDateFormat formatterDate= new SimpleDateFormat("yyyy-MM-dd z");
    static SimpleDateFormat formatterTime= new SimpleDateFormat("[HH-mm-ss-SSS]");

    public static void main(String[] args){
        log("hello");
        log("hello");
    }

    public static CompletableFuture<Message> sendDiscordMessage(TextChannel ch, String key, Object ...args) {
         return ch.sendMessage(translate(key, args));
    }

    public static String translate(String key, Object ...args) {
        return Text.cleanColors(Text.format(bundle.getDefault(key), args));
    }

    public static void info(String key, Object ...args) {
        Log.info(translate(key, args));
    }

    public static void log(String message) {
        log(new RuntimeException(message));
    }

    public static void sendMessage(String key, Object ...args) {
        for(PD pd : db.online.values()) {
            pd.sendServerMessage(key, args);
        }
    }

    public static void log(Runnable run) {
        try {
            run.run();
        } catch (Exception e) {
            log(e);
        }
    }

    public static void log(Throwable t) {
        String ex = ExceptionUtils.readStackTrace(t);
        Date date = new Date(System.currentTimeMillis());
        File f = new File(outDir+formatterDate.format(date));
        t.printStackTrace();
        try {
            Json.makeFullPath(f.getAbsolutePath());
            if(!f.exists()) {
                f.createNewFile();
            }
            PrintWriter out = new PrintWriter(new FileOutputStream(f, true));
            out.println(formatterTime.format(date));
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

    public static void run(Object event, Runnable listener) {
        Events.run(event, () -> {
            try{
                listener.run();
            } catch(Exception ex) {
                log(ex);
            }
        });
    }
}
