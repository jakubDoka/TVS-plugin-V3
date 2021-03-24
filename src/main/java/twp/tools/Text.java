package twp.tools;

import arc.files.*;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.ContentLoader;
import mindustry.type.Item;
import twp.game.Loadout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.min;

// Text formatting utility
public class Text {
    static int[] itemIcons = new int[]{
            63536, 63544, 63543,
            63541, 63539, 63538,
            63537, 63535, 63534,
            63533, 63532, 63531,
            63540, 63530, 63529,
            63542
    };

    public static String clean(String string, String  begin, String  end){
        int fromBegin = 0,fromEnd = 0;
        while (string.contains(begin)){
            int first=string.indexOf(begin,fromBegin),last=string.indexOf(end,fromEnd);
            if(first==-1 || last==-1) break;
            if(first>last){
                fromBegin=first+1;
                fromEnd=last+1;
            }
            string=string.substring(0,first)+string.substring(last+1);
        }
        return string;
    }

    public static String cleanEmotes(String string){
        return clean(string,"<",">");
    }

    public static String cleanColors(String string){
        return clean(string,"[","]");
    }

    public static String cleanName(String name){
        name = cleanColors(name);
        name = cleanEmotes(name);
        return name.replace(" ","_");
    }

    public static String format(String str, Object ...args) {
        try {
            return String.format(str, args);
        } catch (Exception e) {
            return str + "\n[orange]There is a incorrect formatting in bundle. Please report this.";
        }
    }

    public static String milsToTime(long mils){
        long sec = mils / 1000;
        long min = sec / 60;
        long hour = min / 60;
        long days = hour / 24;
        return String.format("%d:%02d:%02d:%02d", days%365 ,hour%24 ,min%60 ,sec%60 );
    }

    public static String formPage(ArrayList<String> data, int page, String title, int pageSize) {
        StringBuilder b = new StringBuilder();
        int pageCount = (int) Math.ceil(data.size() / (float) pageSize);

        page = Mathf.clamp(page, 1, pageCount) - 1;

        int start = page * pageSize;
        int end = min(data.size(), (page + 1) * pageSize);

        b.append("[orange]==").append(title.toUpperCase()).append("(").append(page + 1).append("/");
        b.append(pageCount).append(")==[]\n\n");

        for (int i = start; i < end; i++) {
            b.append(data.get(i)).append("\n");
        }

        return b.toString();
    }

    public static String secToTime(int sec){
        return String.format("%02d:%02d",sec/60,sec%60);
    }

    public static String itemIcon(Item i) {
        int idx = 0;
        for(Field f : Items.class.getFields()) {
            try {
                System.out.println(f.get(null));
                System.out.println(i);
                if (f.get(null).equals(i)) {
                    return String.valueOf((char)itemIcons[idx]);
                }
            } catch (Exception ignore) {}
            idx++;
        }

        return null;
    }

    public static void main(String[] args) throws IllegalAccessException {
        Vars.content = new ContentLoader();
        new Items().load();
        int idx = 0;
        Loadout l = new Loadout(null);
        System.out.println(l.itemsList());
        for(Field f : Items.class.getFields()) {
            System.out.println(String.format("put(\"%s\", Items.%s);", ((Item)f.get(null)).name, f.getName()));
            idx++;
        }
    }

    public static NoSuchFieldException formatInvalidField(String what, String name, String command) {
        return new NoSuchFieldException(String.format("%s with name %s does not exist, use 'content %s' to view options", what, name, command));
    }

    public static <T> String listFields(Class<T> c) {
        StringBuilder sb = new StringBuilder();
        for (Field f : c.getFields()) {
            sb.append(f.getName()).append(" ");
        }
        return  sb.toString();
    }

    /*

     */
}
