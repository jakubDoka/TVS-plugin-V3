package twp.tools;

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
            return str;
        }
    }
}
