package grp07;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.types.ObjectId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CloudToCluster {
    private static final String MONGO_CLOUD_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String MONGO_CLOUD_DB = "g07";
    private static final String MONGO_LOCAL_URI = "mongodb://127.0.0.1:27017";
    private static final String MONGO_LOCAL_DB = "g07";
    //private static final String MONGO_LOCAL_URI = "mongodb://aluno:aluno@madrugadao-sama.ddns.net/g07?authSource=admin&authMechanism=SCRAM-SHA-1";

    private final ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public static void main(String[] args) {

        String[] collectionNames = {
                "sensort1",
                "sensort2",
        };

        new CloudToCluster(collectionNames);
    }

    CloudToCluster(String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        for (String collectionName : collectionNames)
            this.buffer.put(collectionName, new LinkedBlockingQueue<>());

        new MongoCloudFetcher(MONGO_CLOUD_URI, MONGO_CLOUD_DB).deal(this.getBuffer());
        new MongoClusterPublisher(MONGO_LOCAL_URI, MONGO_LOCAL_DB).deal(this.getBuffer());
    }

    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return this.buffer;
    }

    private static class MongoCloudFetcher extends MongoHandler<Measurement> {
        private static final int SLEEP_TIME = 5000;

        public MongoCloudFetcher(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
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
                                    System.out.println("Fetched: " + doc.getId());
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
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((name, buffer) -> {
                MongoCollection<Measurement> collection = getCurrentDb().getCollection(name, Measurement.class);
                new Thread(() -> {
                    while (true) {
                        try {

                            InsertOneResult res = collection.insertOne(buffer.take());
                            System.out.println("Inserted: " + res.getInsertedId());

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            });
        }
    }
}