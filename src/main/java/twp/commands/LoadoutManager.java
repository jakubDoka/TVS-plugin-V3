package twp.commands;

import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.blocks.storage.*;
import twp.database.enums.*;
import twp.democracy.*;
import twp.game.*;

import java.lang.reflect.*;
import java.util.*;

import static twp.Main.*;

public class LoadoutManager extends Command {

    public Voting store = new Voting(this, "store", 2, 4){{
        protection = Perm.loadout;
        increase = Stat.loadoutVotes;
    }};
    public Voting get = new Voting(this, "get", 2, 3) {{
        protection = Perm.loadout;
        increase = Stat.loadoutVotes;
    }};

    public LoadoutManager() {
        name = "l";
        argStruct = "<get/store/info> [item] [amount]";
        description = "When your core is overflowing with resources you can store them in loadout for later withdrawal.";
    }

    @Override
    public void run(String id, String... args) {
        if(wrongOption(0, args, "info get store")) return;

        // handle info
        if (args.length == 1 && args[0].equals("info")) {
            // format page
            StringBuilder sb = new StringBuilder();
            if(testMode || caller == null) { // testing stuff
                for(Item i : db.loadout.items.values()) {
                    sb.append(i.name).append(": ").append(db.loadout.amount(i)).append("\n");
                }
                Log.info(sb.toString());
            } else {
                for(Item i : db.loadout.items.values()) {
                    sb.append(db.loadout.amount(i)).append(db.loadout.itemIcons.get(i.name)).append("\n");
                }
                caller.sendInfoMessage("l-info", sb.toString());
            }
            result = Result.none;
            return;
        } else if(checkArgCount(args.length, 3)) {
            return;
        }

        if(isNotInteger(args, 2)) {
            return;
        }

        final int a = Integer.parseInt(args[2]);

        Seq<Item> items = parseItem(args[1]);
        if(items.size == 0) {
            result = Result.invalidRequest;
            setArg(db.loadout.itemsList());
            return;
        }

        switch (args[0]) {
            case "get":
                if(!docks.canUse()) { // no free ships
                    result = Result.penalty;
                    return;
                }

                Item i = items.first();
                if(db.loadout.amount(i) == 0) { // useless, nothing to transport
                    result = Result.redundant;
                    return;
                }

                result = get.pushSession(caller, session -> {
                    // to prevent negative values
                    int amount = Math.min(a, (int)db.loadout.amount(i));
                    while (amount != 0) {
                        if(!docks.canUse()) {
                            break;
                        }

                        // take until possible and avoid negative values
                        int rAmount = amount;
                        if(amount < config.loadout.shipCapacity) {
                            amount = 0;
                        } else {
                            rAmount = config.loadout.shipCapacity;
                            amount -= rAmount;
                        }

                        // do transaction and formatting
                        String stack = db.loadout.stackToString(i, rAmount);
                        int finalRAmount = rAmount;
                        db.loadout.inc(i, -rAmount);

                        docks.use( new Docks.Ship(stack+Docks.Ship.itemsToCore, () -> {
                            CoreBlock.CoreBuild core = Loadout.core();
                            if(core == null) { // all cores gon, ship go bay bay
                                hud.sendMessage("l-shipIsLost", new Object[]{stack}, 10, "red", "gray");
                                return;
                            }

                            // do transaction and return
                            core.items.add(i, finalRAmount);
                            docks.use(new Docks.Ship("returning", () -> {}, config.loadout.shipTravelTime));
                        }, config.loadout.shipTravelTime));
                    }
                }, db.loadout.stackToString(i, Math.min((config.shipLimit-docks.ships.size) * config.loadout.shipCapacity, a)));
                break;
            case "store":
                CoreBlock.CoreBuild core = Loadout.core();
                if(core == null) { // no core no resorces
                    result = Result.fail;
                    return;
                }

                // create icon list
                StringBuilder s = new StringBuilder();
                for(Item item : items) {
                    s.append(db.loadout.itemIcons.get(item.name));
                }

                result = store.pushSession(caller, session -> {
                    // summarize transport results, do transaction
                    StringBuilder sb = new StringBuilder();
                    for(Item item : items) {
                        int am = Math.min(a, core.items.get(item));
                        sb.append(db.loadout.stackToString(item, am)).append(" ");
                        core.items.remove(item, am);
                        db.loadout.inc(item, am);
                    }
                    hud.sendMessage("l-itemSend", new Object[]{sb.toString()}, 10, "white");
                }, a + s.toString());
                break;
        }
    }

    public Seq<Item> parseItem(String raw) {
        Seq<Item> items = new Seq<>();
        if(raw.equals("all")) {
            for (Item i : db.loadout.items.values()) {
                items.add(i);
            }
            return items;
        }

        for(String s : raw.split("/")) {
            Item i = db.loadout.items.get(s);
            if (i != null) {
                items.add(i);
            }
        }

        return items;
    }

    public static LoadoutManager game = new LoadoutManager();

    public static void main(String[] args){
        System.out.println(hasRepetition("aagdjoaskdka", 3));
    }

    static class IntPtr {
        int value;
    }

    static boolean hasRepetition(String s, int amount) {
        HashMap<Character, IntPtr> map = new HashMap<>();
        for(int i = 0; i < s.length(); i++) {
            IntPtr ip = map.computeIfAbsent(s.charAt(i), k -> new IntPtr());
            ip.value++;
            if(ip.value >= amount) {
                return true;
            }
        }

        return false;
    }
}
