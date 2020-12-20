package twp.tools;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Json {
    public static <V> HashMap<String, V> loadHashmap(String filename, Class<V> val, HashMap<String, V> def) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType jt = mapper.getTypeFactory().constructMapLikeType(HashMap.class, String.class, val);
        File fi = new File(filename);
        if(!fi.exists()) {
            if (def == null) {
                return null;
            }
            saveSimple(filename, def);
            return def;
        }
        try {
            return mapper.readValue(new File(filename), jt);
        } catch (IOException e) {
            Logging.info("json-failLoad", filename, e.getMessage());
            Logging.log(e);
            return null;
        }
    }

    public static void saveSimple(String filename, Object obj){
        ObjectMapper mapper = new ObjectMapper();
        try {
            makeFullPath(filename);
            mapper.writeValue(new File(filename), obj);
        } catch (IOException e) {
            Logging.info("json-failSave", filename, e.getMessage());
        }
    }

    public static void makeFullPath(String filename) throws IOException {
        File targetFile = new File(filename);
        File parent = targetFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Couldn't create dir: " + parent);
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
        } catch (IOException e){
            Logging.info("json-failLoad", filename, e.getMessage());
            return null;
        }
    }

    public static <T> T saveJackson(String filename, Class<T> type){
        ObjectMapper mapper = new ObjectMapper();
        try {
            makeFullPath(filename);
            File f = new File(filename);
            T obj = type.getDeclaredConstructor().newInstance();
            mapper.writeValue(f, obj);
            return obj;
        } catch (Exception e){
            Logging.info("json-failSave", filename, e.getMessage());
        }
        return null;
    }
}
