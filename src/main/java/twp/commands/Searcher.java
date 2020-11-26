package twp.commands;

import arc.func.Cons;
import arc.math.Mathf;
import arc.util.Strings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import twp.database.enums.RankType;
import twp.database.Account;
import twp.database.enums.Stat;
import twp.tools.Enums;

import java.io.IOException;

import static twp.Main.db;
import static twp.Main.ranks;

// Searcher interfaces with database and allows reading public data about players for players
// though is mainly supports quick search for admins for fast actions
public class Searcher extends Command {
    // limit to haw match data command can show to prevent spam on discord
    int showLimit = 20;

    public Searcher() {
        name = "search";
        argStruct = "<name-filter/none/online> [property] [slice] [inverted]";
        description = "lets you query trough tws database. Its fastest way to figure out ID. You can also compare your stats with others.";
    }

    @Override
    void run(String id, String... args) {
        StringBuilder sb = new StringBuilder();

        if(args[0].equals("online")) {
            if(db.online.isEmpty()) {
                result = Result.noOneOnline;
                return;
            }

            db.online.forEachValue(iter -> sb.append(iter.next().getAccount().summarize(Stat.playTime)).append("\n"));

            setArg(sb.toString());
            result = Result.successOnline;
            return;
        }

        FindIterable<Document> found;
        int count = 0;
        if (args[0].endsWith("none"))  {
            found = db.handler.all();
            count = db.getDatabaseSize();
        } else {
            found = db.handler.startsWith("name", args[0]);
            for(Document ignored : found) {
                count++; // fucking iterators
            }
        }

        Stat stat = Stat.playTime;
        if(args.length > 1) {
            RankType rankType = ranks.rankType(args[1]);
            if (rankType == null) {
                if(!Enums.contains(Stat.class, args[1])) {
                    setArg(
                            Enums.list(Stat.class),
                            ranks.rankList(RankType.rank),
                            ranks.rankList(RankType.specialRank),
                            ranks.rankList(RankType.donationRank)
                    );
                    result = Result.wrongOption;
                    return;
                }
                stat = Stat.valueOf(args[1]);
            } else {
                found = found.filter(Filters.eq(rankType.name(), args[1]));
                count = 0;
                for(Document ignored : found) {
                    count++;
                }
            }
        }

        if(args.length == 4) {
            found = found.sort(Sorts.descending(stat.name()));
        } else {
            found = found.sort(Sorts.ascending(stat.name()));
        }

        Slice slice;
        if(args.length > 2) {
            try {
                slice = new Slice(args[2], count);
            } catch (IOException e) {
                result = Result.invalidSlice;
                return;
            }
        } else {
            slice = new Slice(0, showLimit, count);
        }

        if(slice.empty()) {
            result = Result.emptySlice;
            return;
        }

        Stat finalStat = stat;
        slice.forEach(found, (doc) -> {
            Account account = new Account(doc);
            sb.append(account.summarize(finalStat)).append("\n");
        });

        setArg(sb.toString(), slice.len(), count, (float) slice.len() / (float)count * 100);
        result = Result.success;
    }

    public static Searcher
            terminal = new Searcher() {{showLimit = 100;}},
            game = new Searcher() {{showLimit = 40;}},
            discord = new Searcher() {{showLimit = 20;}};

    static class Slice {
        int[] ends;

        static int[] slice(int start, int end, int max) {
            int[] ends = new int[]{start, end};

            for(int i = 0; i < ends.length; i++) {
                if(ends[i] < 0) {
                    ends[i] = max + ends[i] + 1;
                }
                ends[i] = Mathf.clamp(ends[i], 0, max);
            }

            if(ends[0] > ends[1]) {
                int temp = ends[0];
                ends[0] = ends[1];
                ends[1] = temp;
            }

            return ends;
        }

        Slice(int start, int end, int max) {
            ends = slice(start, end, max);
        }

        Slice(String raw, int max) throws IOException {
            String[] parts = raw.split("=");
            if(parts.length != 2 || !Strings.canParseInt(parts[0]) || !Strings.canParseInt(parts[0])) {
                throw new IOException();
            }
            ends = slice(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), max);
        }

        boolean empty() {
            return len() == 0;
        }

        int len() {
            return Math.abs(ends[0] - ends[1]);
        }

        void forEach(FindIterable<Document> arr, Cons<Document> con) {
            int i = 0;
            for(Document doc : arr) {
                if(i >= ends[0] && i < ends[1]) {
                    con.get(doc);
                }
                i++;
            }
        }
    }
}
