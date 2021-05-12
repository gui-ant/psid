package grp07;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import common.MongoConnector;
import org.bson.Document;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


//  CASO A DB NAO ESTEJA ACESSIVEL!!!!!!!


public abstract class ConnectToMongo<T> extends MongoConnector {

    private final ConcurrentHashMap<String, LinkedBlockingQueue<T>> collectionsDataBuffer = new ConcurrentHashMap<>();

    public ConnectToMongo(String sourceUri, String db) {
        super(sourceUri, db);
        deal(collectionsDataBuffer);
        System.out.println("Connected to the database successfully");
    }

    protected abstract void deal(ConcurrentHashMap<String, LinkedBlockingQueue<T>> collectionsDataBuffer);

    protected T getLastObject(MongoCollection<T> collection) {
        System.out.println(collection.find().sort(new Document("_id", -1)).limit(1));
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

    public ConcurrentHashMap<String, LinkedBlockingQueue<T>> getFetchingSource() {
        return collectionsDataBuffer;
    }

    protected MongoCollection<T> getCollection(String name, Class<T> type) {
        return getCurrentDb().getCollection(name, type);
    }

   /* public void startFetching() {
        // Para cada collection lança uma thread
        collectionsDataBuffer.forEach((collection, buffer) -> {
            new Thread(new MeasureFetcher(getMeasureCollection(collection), buffer)).start();
        });
    }*/

   /* public void startPublishing(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new MongoCollectionHandler(getMeasureCollection(collectionName), buffer).start();
                }
        );
    }*/
}
