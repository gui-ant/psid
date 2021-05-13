package grp07;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import common.ClientToClient;
import org.bson.types.ObjectId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CloudToCluster implements ClientToClient {
    private static final String SOURCE_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String TARGET_URI_CLUSTER = "mongodb://127.0.0.1:27017";
    //private static final String TARGET_URI_MADRUGADAO = "mongodb://aluno:aluno@madrugadao-sama.ddns.net/g07?authSource=admin&authMechanism=SCRAM-SHA-1";
    private static final String SOURCE_DB = "g07";
    private static final String TARGET_DB = "g07";

    public static void main(String[] args) {

        String[] collectionNames = {
                "sensort1",
                "sensort2",
        };
        CloudToCluster ctc = new CloudToCluster(collectionNames);

        ctc.startFetching();
        ctc.startPublishing();
    }


    private final ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    CloudToCluster(String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        for (String collection : collectionNames)
            this.buffer.put(collection, new LinkedBlockingQueue<>());
    }


    @Override
    public void startFetching() {

        new MongoFetcher(SOURCE_URI_ATLAS, SOURCE_DB).deal(this.getBuffer());
    }

    @Override
    public void startPublishing() {
        new MongoPublisher(TARGET_URI_CLUSTER, TARGET_DB).deal(this.getBuffer());
    }

    @Override
    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return this.buffer;
    }

    private class MongoFetcher extends MongoHandler<Measurement> {
        private static final int SLEEP_TIME = 5000;

        public MongoFetcher(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, measurements) -> {
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
                }).start();
            });
        }
    }

    private class MongoPublisher extends MongoHandler<Measurement> {

        public MongoPublisher(String sourceUri, String db) {
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