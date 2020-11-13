package twp.tools;

import twp.Global;
import org.apache.commons.codec.digest.DigestUtils;

public class Security {
    public static long hash(String password) {
        password += Global.config.salt;
        long res = 0;
        for(int i = 0; i < password.length(); i++) {
            res = res + (long)Math.pow(password.charAt(i), 2);
        }
        return res;
    }

    public static String hash2(String password) {
        return DigestUtils.sha256Hex(password + Global.config.salt);
    }

}
