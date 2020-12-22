package twp.database.core;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import twp.database.enums.Stat;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.and;

public class Handler {
    protected MongoCollection<Document> data;
    protected MongoCollection<Document> counter;

    public Handler(MongoCollection<Document> data, MongoCollection<Document> counter){
        this.data = data;
        this.counter = counter;
    }

    public Bson idFilter(long id){
        return Filters.eq("_id", id);
    }

    public void delete(long id){
        data.deleteOne(idFilter(id));
    }

    public void set(long id, String field, Object value) {
        data.updateOne(idFilter(id), Updates.set(field, value));
    }

    public void unset(long id, String field) {
        data.updateOne(idFilter(id), Updates.unset(field));
    }

    // Pull removes value from array in database document
    public void pull(long id, String field, Object value) {
        data.updateOne(idFilter( id), Updates.pull(field, value));
    }

    public boolean contains(long id, String field, Object value) {
        return data.find(and(idFilter( id), Filters.eq(field, value))).first() != null;
    }


    public Object get(long id, String field) {
        Document dc = data.find(idFilter( id)).first();
        if (dc == null) return null;
        return dc.get(field);
    }

    public void inc(long id, Stat stat, long amount){
        data.updateOne(idFilter( id), Updates.inc(stat.name(), amount));
    }

    public void incOne(long id, Stat stat) {
        inc( id, stat, 1);
    }

    public Long getStat(long id, String stat) {
        Long val = (Long) get( id, stat);
        return val == null ? 0 : val;
    }

    public void setStat(long id, Stat stat, long value) {
        set(id, stat.name(), value);
    }

    public FindIterable<Document> gt(Document doc, String stat) {
        return data.find(Filters.gt(stat, doc.get(stat)));
    }

    public FindIterable<Document> find(Bson filter) {
        return data.find(filter);
    }

    public FindIterable<Document> all() {
        return data.find();
    }

    public FindIterable<Document> startsWith(String field, String sub) {
        Pattern pattern = Pattern.compile("^"+Pattern.quote(sub), Pattern.CASE_INSENSITIVE);
        return data.find(Filters.regex(field, pattern));
    }

    // newID creates new incremented id
    public long newId() {
        if(counter.updateOne(idFilter(0), Updates.inc("id", 1)).getModifiedCount() == 0){
            long id = 0;
            Document latest = data.find().sort(new Document("_id", -1)).first();
            if(latest != null) {
                id = (long)latest.get("_id");
                if (id == -1) {
                    id = 0;
                }
            }
            counter.insertOne(new Document("_id", 0).append("id",id));
        }
        Document counter = this.counter.find().first();
        if(counter == null){
            throw new IllegalStateException("Well then this is fucked.");
        }
        return (long) counter.get("id");
    }

    // For testing purposes
    public void drop() {
        data.drop();
        counter.drop();
    }
}
