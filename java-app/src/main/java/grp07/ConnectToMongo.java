package grp07;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import common.MongoConnector;
import common.MongoFetcher;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

//  CASO A DB NAO ESTEJA ACESSIVEL!!!!!!!

public class ConnectToMongo extends MongoConnector {
    private final ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer = new ConcurrentHashMap<>();

    public ConnectToMongo(String sourceUri) {
        super(sourceUri);
        System.out.println("Connected to the database successfully");
    }

    public ConnectToMongo(String sourceUri, String sourceDatabase) {
        this(sourceUri);
        useDatabase(sourceDatabase);
    }

    public void useCollections(String[] collectionNames) {
        collectionsDataBuffer.clear();
        for (String col : collectionNames)
            collectionsDataBuffer.put(col, new LinkedBlockingQueue<>());
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getCollectionsBuffer() {
        return collectionsDataBuffer;
    }

    public void startFetching() {
        // Para cada collection lança uma thread
        collectionsDataBuffer.forEach((collection, buffer) -> {
            new Thread(new MeasurementFetcher(getMeasureCollection(collection), buffer)).start();
        });
    }

    public void startPublishing(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new MeasurePublisher(getMeasureCollection(collectionName), buffer).start();
                }
        );
    }

    private MongoCollection<Measurement> getMeasureCollection(String collectionName) {
        return getCurrentDB().getCollection(collectionName, Measurement.class);
    }

    private class MeasurementFetcher extends MongoFetcher<Measurement> {
        private static final int SLEEP_TIME = 5000;

        public MeasurementFetcher(MongoCollection<Measurement> collection, LinkedBlockingQueue<Measurement> buffer) {
            super(collection, buffer);
        }

        @Override
        protected Class<Measurement> getMapperClass() {
            return Measurement.class;
        }

        @Override
        protected Measurement getLastObject(MongoCollection<Measurement> collection) {
            return collection.find().sort(new Document("_id", -1)).limit(1).first();
        }

        @Override
        public void run() {

            Measurement doc = getLastObject(super.getCollection());

            // TODO: Considerar a collection estar vazia,i.e. gerar doc = null
            ObjectId lastId = doc.getId();
            while (true) {
                try {
                    System.out.println("Fetching " + getCollectionName(super.getCollection()) + "...");

                    MongoCursor<Measurement> cursor = getCollection().find(Filters.gt("_id", lastId)).iterator();

                    // Le os novos dados e adiciona-os ao buffer
                    while (cursor.hasNext()) {
                        doc = cursor.next();
                        lastId = doc.getId();
                        getBuffer().offer(doc);
                        System.out.println("Fetched: " + doc.getId());
                    }
                    sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class MeasurePublisher extends Thread {
        private final MongoCollection<Measurement> collection;
        private final LinkedBlockingQueue<Measurement> buffer;

        MeasurePublisher(MongoCollection<Measurement> collection, LinkedBlockingQueue<Measurement> buffer) {
            this.collection = collection;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    InsertOneResult res = this.collection.insertOne(buffer.take());
                    System.out.println("Inserted: " + res.getInsertedId());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
