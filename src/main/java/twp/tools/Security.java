package main.java.twp.tools;

import main.java.twp.Global;
import org.apache.commons.codec.digest.DigestUtils;

import static main.java.twp.Main.config;

// some hash functions
public class Security {
    // ignore this big mistake pls
    public static long hash(String password) {
        password += config.salt;
        long res = 0;
        for(int i = 0; i < password.length(); i++) {
            res = res + (long)Math.pow(password.charAt(i), 2);
        }
        return res;
    }

    public static String hash2(String password) {
        return DigestUtils.sha256Hex(password + config.salt);
    }

}
