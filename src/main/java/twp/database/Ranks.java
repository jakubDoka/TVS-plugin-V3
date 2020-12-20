package twp.database;

import arc.graphics.Color;
import twp.Global;
import twp.database.enums.Perm;
import twp.database.enums.RankType;
import twp.database.enums.Stat;
import twp.tools.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import static twp.tools.Json.saveSimple;

// Ranks contains all ranks available to players
// further explanation in class
public class Ranks {
    static final String rankFile = Global.config_dir + "specialRanks.json";

    // buildIn are ranks that should be always present, you still can customize them
    public HashMap<String, Rank> buildIn = new HashMap<>();
    // special ranks can be created via specialRanks.json
    public HashMap<String, Rank> special = new HashMap<>();
    // donation ranks can not be achieved by game progress, only by donating, thus custom ranks without quests
    // goes here
    public HashMap<String, Rank> donation = new HashMap<>();

    // Build in ranks for nicer access as they are always present
    public Rank griefer, paralyzed, newcomer, verified, candidate, admin;

    // Error is placeholder - in case players rank is unresolved, It should disappear after re-log
    public Rank error = new Rank(true, false, "error", "red", new HashMap<String, String>() {{
        put("default", "When your special rank disappears you have chance to get this super rare rank");
    }}, 0, null, null, null, null);

    public Ranks() {
        loadBuildIn();
        loadRanks();
    }

    public void loadBuildIn() {
        griefer = new Rank(true, false, "griefer", "#ff6bf8",
                new HashMap<String, String>() {{
                    put("default", "Best fit for any impostor.");
                }}, 0, null, null, null, null);
        paralyzed = new Rank(true, false, "paralyzed", "#ff9e1f",
                new HashMap<String, String>() {{
                    put("default", "This is placeholder in case you have to reload your account.");
                }}, 0, null, null, null, null);
        newcomer = new Rank(false, false, "newcomer", "#b3782d",
                new HashMap<String, String>() {{
                    put("default", "This is first rank you will get.");
                }}, 0, new HashSet<>(Collections.singletonList(Perm.normal.name())), null, null, null);
        verified = new Rank(false, false, "verified", "#2db3aa",
                new HashMap<String, String>() {{
                    put("default", "Pass the test and you ll get this. Protects your blocks against newcomers.");
                }}, 1, new HashSet<>(Collections.singletonList(Perm.high.name())), null, null, null);
        candidate = new Rank(true, false, "candidate", "#1d991d" ,
                new HashMap<String, String>() {{
                    put("default", "This is middle step between normal player and admin.");
                }}, 2, new HashSet<>(Collections.singletonList(Perm.higher.name())), null, null, null);
        admin =  new Rank(true, true, "admin", "#2930c2" ,
                new HashMap<String, String>() {{
                    put("default", "You have power to protect others.");
                }}, 3, new HashSet<>(Collections.singletonList(Perm.highest.name())), null, null, null);

        buildIn.put(griefer.name, griefer);
        buildIn.put(paralyzed.name, paralyzed);
        buildIn.put(newcomer.name, newcomer);
        buildIn.put(verified.name, verified);
        buildIn.put(candidate.name, candidate);
        buildIn.put(admin.name, admin);
    }

    // if loadRanks fails it restarts everything to default because ranks can depend on each other
    // Its then easier to verify if rank links are correct
    public void loadRanks() {
        special.clear();
        donation.clear();

        HashMap<String, Rank[]> ranks = Json.loadHashmap(rankFile, Rank[].class, defaultRanks);
        if (ranks == null) return;
        Rank[] srs = ranks.get("ranks");
        if (srs == null) return;

        // To get rid of repetition
        Runnable end = () -> {
            special.clear();
            donation.clear();
            loadBuildIn();
            Logging.info("ranks-fileInvalid");
        };

        for (Rank r : srs) {
            // verify permissions
            if(r.permissions != null) {
                for(String p : r.permissions) {
                    if(Enums.log(Perm.class, p)) {
                        end.run();
                        return;
                    }
                }
            }
            // quest is not important for build in rank
            if(buildIn.containsKey(r.name)) {
                buildIn.put(r.name, r);
                continue;
            }
            // verify quest
            if (r.quests != null) {
                for (String s : r.quests.keySet()) {
                    if(Enums.log(Stat.class, s)) {
                        end.run();
                        return;
                    }
                    for (String l : r.quests.get(s).keySet()) {
                        if(Enums.log(Rank.Mod.class, l)) {
                            end.run();
                            return;
                        }
                    }
                }
            } else { // no quests so add it to donations instead
                donation.put(r.name, r);
                continue; // links are not important for donation ranks
            }

            // verify links
            if (r.linked != null) {
                for (String l : r.linked) {
                    if (!ranks.containsKey(l)) {
                        Logging.info("ranks-missing", l);
                        end.run();
                        return;
                    }

                }
            }
            special.put(r.name, r);
        }
    }

    public static HashMap<String, Rank[]> defaultRanks = new HashMap<String, Rank[]>(){{
        put("ranks", new Rank[]{
            new Rank() {
                {
                    name = "kamikaze";
                    color = "scarlet";
                    description = new HashMap<String, String>() {{
                        put("default", "put your description here.");
                        put("en_US", "Put translation like this.");
                    }};
                    value = 1;
                    permissions = new HashSet<String>() {{
                        add(Perm.suicide.name());
                    }};
                    quests = new HashMap<String, HashMap<String, Integer>>() {{
                        put(Stat.deaths.name(), new HashMap<String, Integer>() {{
                            put(Mod.best.name(), 10);
                            put(Mod.required.name(), 100);
                            put(Mod.frequency.name(), 20);
                        }});
                    }};
                }
            },
            new Rank() {{
                name = "donor";
                color = "#" + Color.gold.toString();
                description = new HashMap<String, String>() {{
                    put("default", "For people who support server financially.");
                }};
                permissions = new HashSet<String>() {{
                    add(Perm.colorCombo.name());
                    add(Perm.suicide.name());
                }};
                pets = new ArrayList<String>() {{
                    add("fire-pet");
                    add("fire-pet");
                }};
            }}
        });
    }};

    public Rank getRank(String name, RankType type) {
        return getRanks(type).getOrDefault(name, type == RankType.rank ? newcomer : error);
    }

    // ranksList prints all ranks of same type
    public String rankList(RankType type) {
        if (getRanks(type).isEmpty()) {
            return " none\n";
        }
        StringBuilder b = new StringBuilder();
        for(Rank s : getRanks(type).values()) {
            b.append(s.getSuffix()).append(" ");
        }
        return b.substring(0, b.length() - 1);
    }

    // rankType finds out a type of rank, assuming there are no duplicates
    public RankType rankType(String name) {
        for(RankType r : RankType.values()) {
            if (getRanks(r).containsKey(name)) {
                return r;
            }
        }
        return null;
    }

    // getRanks returns Rank group based of tipe
    public HashMap<String, Rank> getRanks(RankType type) {
        switch (type) {
            case rank:
                return buildIn;
            case specialRank:
                return special;
            default:
                return donation;
        }
    }


}
