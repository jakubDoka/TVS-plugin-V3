package twp.tools;

import arc.util.Log;
import twp.database.enums.Perm;
import mindustry.gen.Player;

public class Enums {
    // returns whether enum contains a value
    public static <T extends Enum<T>> boolean contains(Class<T> cl, String value) {
        for(T v : cl.getEnumConstants()){
            if(v.name().equals(value)) return true;
        }
        return false;
    }

    // prints all enum variants
    public static <T extends Enum<T>> String list(Class<T> cl) {
        StringBuilder s = new StringBuilder();
        for (T v : cl.getEnumConstants()) {
            s.append(v).append(", ");
        }
        return s.substring(0, s.length() - 2);
    }

    // combination of contains and list, this also prints available values
    static <T extends  Enum<T>> boolean log(Class<T> cl, String value, Printer p) {
        if(!contains(cl, value)){
            p.run("Permission " + value + " does not exist.");
            p.run("Available: " + list(Perm.class));
            return true;
        }
        return false;
    }

    public static <T extends  Enum<T>> boolean log(Class<T> cl, String value) {
        return log(cl, value, Log::info);
    }

    public static <T extends  Enum<T>> boolean log(Class<T> cl, String value, Player player) {
        return log(cl, value, player::sendMessage);
    }

    interface Printer {
        void run(String message);
    }
}
