package twp.game;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock;
import org.bson.Document;
import twp.tools.Logging;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Loadout {

    public HashMap<String, String> itemIcons = new HashMap<String, String>(){{
        put("scrap", "\uf830");
        put("copper", "\uf838");
        put("lead", "\uf837");
        put("graphite", "\uf835");
        put("coal", "\uf833");
        put("titanium", "\uf832");
        put("thorium", "\uf831");
        put("silicon", "\uf82f");
        put("plastanium", "\uf82e");
        put("phase-fabric", "\uf82d");
        put("surge-alloy", "\uf82c");
        put("spore-pod", "\uf82b");
        put("sand", "\uf834");
        put("blast-compound", "\uf82a");
        put("pyratite", "\uf829");
        put("metaglass", "\uf836");
    }};

    public HashMap<String, Item> items = new HashMap<>();

    MongoCollection<Document> data;

    public Loadout(MongoCollection<Document> data) {
        if(data != null) {
            this.data = data;
            if(storage() == null) {
                data.insertOne(new Document().append("_id", 0));
            }
        }

        for(Field f: Items.class.getFields()) {
            try {
                Item i = (Item)f.get(null);
                items.put(i.name, i);
            } catch (IllegalAccessException e) {
                Logging.log(e);
            }
        }
    }

    public String itemsList() {
        StringBuilder sb = new StringBuilder();
        for(Item i : items.values()) {
            sb.append(String.format("[#%s]%s[] ", i.color.toString(), i.name));
        }

        return sb.toString();
    }

    public String stackToString(Item i, int amount) {
        return String.format("%d%s", amount, itemIcons.get(i.name));
    }

    public static CoreBlock.CoreBuild core() {
        return Vars.state.teams.get(Team.sharded).core();
    }

    private Document storage() {
        return data.find().first();
    }

    public long amount(Item i) {
        Long res = storage().getLong(i.name);
        if(res == null) return 0;
        return res;
    }

    public boolean hes(Item i, long amount) {
        return amount(i) >= amount;
    }

    public void inc(Item i, long amount) {
        data.updateOne(null, Updates.inc(i.name, amount));
    }
}
