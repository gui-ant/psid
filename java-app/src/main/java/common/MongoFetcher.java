package common;

import com.mongodb.client.MongoCollection;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MongoFetcher<T> {

    private final HashMap<String, MongoCollection<T>> collections;
    private final ConcurrentHashMap<String, LinkedBlockingQueue<T>> buffer;
    private static final int SLEEP_TIME = 5000;

    public MongoFetcher(HashMap<String, MongoCollection<T>> collections) {
        this.collections = collections;
        this.buffer = new ConcurrentHashMap<>();

        startFetching();
    }

    protected abstract void startFetching();

    protected abstract T getLastObject(MongoCollection<T> collection);

    public LinkedBlockingQueue<T> getCollectionBuffer(String collection) {
        return buffer.get(collection);
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<T>> getBuffer() {
        return buffer;
    }

    public HashMap<String, MongoCollection<T>> getCollections() {
        return collections;
    }

    protected String getCollectionName(MongoCollection<T> collection) {
        return collection.getNamespace().getCollectionName();
    }
}
