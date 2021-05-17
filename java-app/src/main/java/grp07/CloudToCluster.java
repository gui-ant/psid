package grp07;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import common.IniConfig;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CloudToCluster extends IniConfig {

    private final HashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public static void main(String[] args) {
        new CloudToCluster("config.ini");
    }

    CloudToCluster(String iniFile) {
        super(iniFile);

        String mongoCloudUri = getConfig("mongo", "cloud_uri");
        String mongoCloudDb = getConfig("mongo", "cloud_db");
        String mongoLocalUri = getConfig("mongo", "local_uri");
        String mongoLocalDb = getConfig("mongo", "local_db");
        String[] collectionNames = getConfig("mongo", "collections").split(",");

        this.buffer = new HashMap<>();
        for (String collectionName : collectionNames)
            this.buffer.put(collectionName, new LinkedBlockingQueue<>());

        new MongoCloudFetcher(mongoCloudUri, mongoCloudDb).deal(this.getBuffer());
        new MongoClusterPublisher(mongoLocalUri, mongoLocalDb).deal(this.getBuffer());
    }

    public HashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return this.buffer;
    }

    private static class MongoCloudFetcher extends MongoHandler<Measurement> {
        private static final int SLEEP_TIME = 5000;

        public MongoCloudFetcher(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, measurements) ->
                    new Thread(() -> {
                        MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);
                        Measurement doc = getLastObject(collection);

                        // TODO: Considerar a collection estar vazia,i.e. gerar doc = null
                        ObjectId lastId = doc.getId();
                        while (true) {
                            try {
                                System.out.println("Fetching " + collectionName + "...");

                                MongoCursor<Measurement> cursor = collection.find(Filters.gt("_id", lastId)).iterator();

                                // Le os novos dados e adiciona-os ao buffer
                                while (cursor.hasNext()) {
                                    doc = cursor.next();
                                    lastId = doc.getId();
                                    measurements.offer(doc);
                                    System.out.println("Fetched:\t" + doc);
                                }
                                Thread.sleep(SLEEP_TIME);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start());
        }
    }

    private static class MongoClusterPublisher extends MongoHandler<Measurement> {

        public MongoClusterPublisher(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((name, buffer) -> {
                MongoCollection<Measurement> collection = getCurrentDb().getCollection(name, Measurement.class);
                new Thread(() -> {
                    while (true) {
                        try {
                            Measurement m = buffer.take();
                            InsertOneResult res = collection.insertOne(m);
                            System.out.println("Inserted:\t" + m);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            });
        }
    }
}