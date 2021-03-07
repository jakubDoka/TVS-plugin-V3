package main.java.twp.commands.tests;

import arc.Events;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.core.ContentLoader;
import mindustry.game.EventType;
import org.bson.Document;
import main.java.twp.Main;

import java.io.IOException;

import static main.java.twp.Main.db;

public class Test {
    public static void init() {
        Vars.content = new ContentLoader();
        new Items().load();

        new Main();
        Main.testMode = true;
        Events.fire(new EventType.ServerLoadEvent());
        db.handler.drop();
        db.maps.drop();
    }

    public static void main(String[] args) throws IOException {
        init();
        MongoCollection<Document> coll = db.database.getCollection("test");
        coll.insertOne(new Document());
        coll.updateMany(new Document(), Updates.set("mem.ang", 10));
        System.out.printf(((Document)coll.find().first().get("mem")).toString());
        coll.drop();
    }
}
