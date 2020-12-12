package twp.tools;

import org.junit.platform.commons.util.*;
import twp.*;

import java.io.*;
import java.sql.*;
import java.text.*;

public class Testing {
    static final String outDir = Global.dir + "/errors/";
    public static void main(String[] args){
        Log("hello");
        Log("hello");
    }

    public static void Log(String message) {
        Log(new RuntimeException(message));
    }

    public static void Log(Throwable t) {
        String ex = ExceptionUtils.readStackTrace(t);
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH-mm-ss-SSS z");
        Date date = new Date(System.currentTimeMillis());
        Json.makeFullPath(outDir);
        try (PrintWriter out = new PrintWriter(outDir+formatter.format(date))) {
            out.println(ex);
        } catch(IOException e) { e.printStackTrace();}
    }
}
