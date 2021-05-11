package common;

import com.mongodb.client.MongoCollection;
import grp07.Measurement;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MongoPublisher<T> {

    private final HashMap<String, MongoCollection<Measurement>> collections;
    private final ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public HashMap<String, MongoCollection<Measurement>> getCollections() {
        return collections;
    }

    public LinkedBlockingQueue<Measurement> getCollectionBuffer(String collection) {
        return getBuffer().get(collection);
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return buffer;
    }

    public MongoPublisher(HashMap<String, MongoCollection<Measurement>> collections, ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer) {
        this.collections = collections;
        this.buffer = buffer;
    }

    public abstract void startPublishing();
}