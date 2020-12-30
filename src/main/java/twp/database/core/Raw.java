package twp.database.core;

import twp.database.enums.Stat;
import org.bson.Document;

// common behavior of database document
public class Raw {
    public Document data;

    public Long getStat( Stat stat) {
        return getStat(stat.name());
    }

    public Long getStat(String stat) {
        Long val = (Long) data.get(stat);
        return val == null ? 0 : val;
    }

    public Long getId() {
        return (Long) data.get("_id");
    }

    public String fieldList() {
        StringBuilder sb = new StringBuilder();
        for (String s : data.keySet()) {
            sb.append(s).append(" ");
        }
        return sb.toString();
    }

    public String getName() {
        return (String) data.get("name");
    }

}
