package twp.commands.tests;

import arc.Events;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import mindustry.game.EventType;
import org.bson.Document;
import twp.Main;

import java.io.IOException;

import static twp.Main.db;

public class Test {
    public static void init() {
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
