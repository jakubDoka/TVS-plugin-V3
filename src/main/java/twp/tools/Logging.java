package twp.tools;

import arc.*;
import arc.func.*;
import org.junit.platform.commons.util.*;
import twp.*;

import java.io.*;
import java.sql.*;
import java.text.*;

public class Logging {
    static final String outDir = Global.dir + "/errors/";
    public static void main(String[] args){
        log("hello");
        log("hello");
    }

    public static void log(String message) {
        log(new RuntimeException(message));
    }

    public static void log(Throwable t) {
        String ex = ExceptionUtils.readStackTrace(t);
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss-SSS z");
        Date date = new Date(System.currentTimeMillis());
        Json.makeFullPath(outDir);
        try (PrintWriter out = new PrintWriter(outDir+formatter.format(date))) {
            out.println(ex);
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
