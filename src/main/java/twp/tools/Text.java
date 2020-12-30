package twp.tools;

import arc.math.Mathf;

import java.util.ArrayList;

import static java.lang.Math.min;

public class Text {
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
}
