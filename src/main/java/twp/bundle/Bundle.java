package main.java.twp.bundle;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.twp.database.PD;
import org.jsoup.Jsoup;
import main.java.twp.tools.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

// Some bundle abstraction
public class Bundle {
    static final String bundlePath = "tws-bundles.bundle";
    final Locale locale = new Locale("en","US");
    final ResourceBundle defaultBundle = newBundle(locale);

    ResourceBundle newBundle(Locale locale) {
        return ResourceBundle.getBundle(bundlePath,locale, new UTF8Control());
    }

    HashMap<String, Object> getLocData(String ip) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType jt = mapper.getTypeFactory().constructMapLikeType(HashMap.class, String.class, Object.class);
        try {
            String json = Jsoup.connect("http://ipapi.co/"+ip+"/json").ignoreContentType(true).timeout(3000).execute().body();
            return mapper.readValue(json, jt);
        } catch (IOException e) {
            Logging.log(e);
        }
        return null;
    }

    Locale getLocale(String ip){
        HashMap<String, Object> data = getLocData(ip);
        if(data == null) return locale;

        String languages = (String) data.get("languages");
        if(languages==null) return locale;

        String[] resolvedL = languages.split(",");
        if(resolvedL.length==0) return locale;

        String[] resResolvedL = resolvedL[0].split("-");
        if(resResolvedL.length < 2) return locale;

        return new Locale(resResolvedL[0],resResolvedL[1]);
    }

    public void resolveBundle(PD pd) {
        new Thread(()-> {
            pd.setBundle(newBundle(getLocale(pd.player.ip)));
        });
    }

    public String getDefault(String key) {
        if(defaultBundle.containsKey(key)) {
            return defaultBundle.getString(key);
        }

        return String.format("bundle key %s is missing, please report it", key);
    }

    public static class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
}
