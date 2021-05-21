package grp02;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import common.IniConfig;
import grp07.MongoHandler;
import grp07.Measurement;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionMongoReplics extends IniConfig {

    private final HashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public ConnectionMongoReplics(String iniFile) {
        super(iniFile);
        String mongoCloudUri = getConfig("mongo", "cloud_uri");
        String mongoCloudDb = getConfig("mongo", "cloud_db");
        String mongoLocalUri = getConfig("mongo", "local_uri");
        String mongoLocalDb = getConfig("mongo", "local_db");
        String[] collectionNames = getConfig("mongo", "collections").split(",");

        this.buffer = new HashMap<>();
        for (String collectionName : collectionNames)
            this.buffer.put(collectionName, new LinkedBlockingQueue<>());

        MeasurementsFetcher fetcher = new MeasurementsFetcher(mongoCloudUri, mongoCloudDb);
        MeasurementsPublisher publisher = new MeasurementsPublisher(mongoLocalUri, mongoLocalDb);

        new Thread(() -> {
            while (true) {
                try {
                    fetcher.deal(this.buffer);
                    publisher.deal(this.buffer);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        new ConnectionMongoReplics("config.ini");
    }

    private static class MeasurementsFetcher extends MongoHandler<Measurement> {
        public MeasurementsFetcher(String uri, String db) {
            super(uri, db);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, buffer) -> {
                MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);

                Measurement doc = getLastObject(collection);

                ObjectId lastId = doc.getId();

                MongoCursor<Measurement> cursor = collection.find(Filters.gt("_id", lastId)).iterator();

                // Le os novos dados e adiciona-os ao buffer
                while (cursor.hasNext()) {
                    doc = cursor.next();
                    buffer.offer(doc);
                    System.out.println("Fetched (Mongo):\t" + doc);
                }
            });
        }
    }

    private static class MeasurementsPublisher extends MongoHandler<Measurement> {

        public MeasurementsPublisher(String uri, String db) {
            super(uri, db);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, buffer) -> {
                MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);
                Measurement m;
                while ((m = buffer.poll()) != null) {
                    collection.insertOne(m);
                    System.out.println("Inserted (Mongo):\t" + m);
                }
            });
        }
    }
}