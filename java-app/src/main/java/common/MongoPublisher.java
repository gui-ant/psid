package common;

import com.mongodb.client.MongoCollection;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MongoPublisher<T> {

    private final HashMap<String, MongoCollection<T>> collections;
    private final ConcurrentHashMap<String, LinkedBlockingQueue<T>> buffer;

    public HashMap<String, MongoCollection<T>> getCollections() {
        return collections;
    }

    public LinkedBlockingQueue<T> getCollectionBuffer(String collection) {
        return getBuffer().get(collection);
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<T>> getBuffer() {
        return buffer;
    }

    public MongoPublisher(HashMap<String, MongoCollection<T>> collections, ConcurrentHashMap<String, LinkedBlockingQueue<T>> buffer) {
        this.collections = collections;
        this.buffer = buffer;
    }

    public abstract void startPublishing();
}