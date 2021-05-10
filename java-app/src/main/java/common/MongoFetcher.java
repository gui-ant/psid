package common;

import com.mongodb.client.MongoCollection;
import grp07.Measurement;
import org.bson.Document;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class MongoFetcher<T> extends Thread {

    private final MongoCollection<T> collection;


    private final LinkedBlockingQueue<T> buffer;
    private static final int SLEEP_TIME = 5000;

    public MongoFetcher(MongoCollection<T> collection, LinkedBlockingQueue<T> buffer) {
        this.collection = collection;
        this.buffer = buffer;
    }

    protected abstract Class<T> getMapperClass();


    protected Measurement getLastObject(MongoCollection<Measurement> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    public LinkedBlockingQueue<T> getBuffer() {
        return buffer;
    }

    public MongoCollection<T> getCollection() {
        return collection;
    }

    protected String getCollectionName(MongoCollection<T> collection) {
        return collection.getNamespace().getCollectionName();
    }

}
