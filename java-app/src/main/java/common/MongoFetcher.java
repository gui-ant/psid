package common;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import grp07.ConnectToMongo;
import grp07.Measurement;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MongoFetcher<T> extends ConnectToMongo<T> {
    private static final int SLEEP_TIME = 5000;

    public MongoFetcher(String sourceUri, String db) {
        super(sourceUri, db);
    }

    @Override
    public void deal(ConcurrentHashMap<String, LinkedBlockingQueue<T>> collectionsDataBuffer) {
        collectionsDataBuffer.forEach((collectionName, buffer) ->
            new Thread(() -> {

                MongoCollection<T> collection = getCurrentDb().getCollection(collectionName, getMapperClass());

                T doc = getLastObject(collection);

                // TODO: Considerar a collection estar vazia,i.e. gerar doc = null
                ObjectId lastId = getObjectId(doc);

                while (true) {
                    try {
                        System.out.println("Fetching " + getCollectionName(collection) + "...");

                        MongoCursor<T> cursor = collection.find(Filters.gt("_id", lastId)).iterator();

                        // Le os novos dados e adiciona-os ao buffer
                        while (cursor.hasNext()) {
                            doc = cursor.next();
                            lastId = getObjectId(doc);
                            buffer.offer(doc);
                            System.out.println("Fetched: " + getObjectId(doc));
                        }
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start()
        );
    }

    protected abstract ObjectId getObjectId(T doc);

    protected abstract Class<T> getMapperClass();

}