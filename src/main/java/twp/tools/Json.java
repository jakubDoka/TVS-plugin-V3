package twp.tools;

import arc.util.Log;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Json {
    public static <V> HashMap<String, V> loadHashmap(String filename, Class<V> val, Runnable save) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType jt = mapper.getTypeFactory().constructMapLikeType(HashMap.class, String.class, val);
        File fi = new File(filename);
        if(!fi.exists()) {
            save.run();
            return null;
        }
        try {
            return mapper.readValue(new File(filename), jt);
        } catch (IOException e) {
            Log.info("failed to load config file");
            Logging.log(e);
            return null;
        }
    }

    public static void saveSimple(String filename, Object obj){
        makeFullPath(filename);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(filename), obj);
        } catch (IOException e) {
            Logging.log(e);
        }
    }

    public static void makeFullPath(String filename){
        StringBuilder path = new StringBuilder();
        String[] dirs = filename.split("/");
        for(int i = 0; i<dirs.length-1; i++){
            path.append(dirs[i]).append("/");
            new File(path.toString()).mkdir();
        }
    }

    public static <T> T loadJackson(String filename, Class<T> type){
        ObjectMapper mapper = new ObjectMapper();
        File f = new File(filename);
        try {
            if (!f.exists()){
                return saveJackson(filename, type);
            }
            return mapper.readValue(f, type);
        } catch (IOException ex){
            return null;
        }
    }

    public static <T> T saveJackson(String filename, Class<T> type){
        ObjectMapper mapper = new ObjectMapper();
        makeFullPath(filename);
        File f = new File(filename);
        try {
            T obj = type.newInstance();
            mapper.writeValue(f, obj);
            return obj;
        } catch (Exception e){
            Logging.log(e);
        }
        return null;
    }
}
