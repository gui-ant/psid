package grp07;

import com.mongodb.client.MongoCollection;
import common.MongoConnector;
import org.bson.Document;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MongoHandler<T> extends MongoConnector {

    private final HashMap<String, LinkedBlockingQueue<T>> collectionsDataBuffer = new HashMap<>();

    protected abstract void deal(HashMap<String, LinkedBlockingQueue<T>> collectionsDataBuffer);

    public MongoHandler(String sourceUri, String db) {
        super(sourceUri, db);
        //deal(collectionsDataBuffer);
        System.out.println("Connection stablished with mongo database!");
    }

    protected T getLastObject(MongoCollection<T> collection) {
        return collection.find().sort(new Document("_id", -1)).limit(1).first();
    }

    protected String getCollectionName(MongoCollection<T> collection) {
        return collection.getNamespace().getCollectionName();
    }

    public void useCollections(String[] collectionNames) {
        collectionsDataBuffer.clear();
        for (String col : collectionNames)
            collectionsDataBuffer.put(col, new LinkedBlockingQueue<>());
    }

    public HashMap<String, LinkedBlockingQueue<T>> getFetchingSource() {
        return collectionsDataBuffer;
    }

    protected MongoCollection<T> getCollection(String name, Class<T> type) {
        return getCurrentDb().getCollection(name, type);
    }

}
