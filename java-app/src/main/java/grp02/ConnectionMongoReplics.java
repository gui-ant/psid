package grp02;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import common.ClientToClient;
import grp07.ConnectToMongo;
import grp07.Measurement;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionMongoReplics implements ClientToClient<Measurement> {

    private static final String SOURCE_URI = "mongodb://aluno:aluno@194.210.86.10/?authSource=admin&authMechanism=SCRAM-SHA-1";
    private static final String SOURCE_DB = "sid2021";

    private static final String TARGET_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String TARGET_DB = "g07";

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public ConnectionMongoReplics(String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        for (String collectionName : collectionNames)
            this.buffer.put(collectionName, new LinkedBlockingQueue<>());
    }

    public static void main(String[] args) {

        String[] collectionNames = {"sensort1"};

        ConnectionMongoReplics cmr = new ConnectionMongoReplics(collectionNames);

        new Thread(() -> {
            while (true) {
                try {
                    cmr.startFetching();
                    cmr.startPublishing();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void startFetching() {
        new MeasurementsFetcher(SOURCE_URI, SOURCE_DB).deal(this.buffer);
    }

    @Override
    public void startPublishing() {
        new MeasurementsPublisher(TARGET_URI_ATLAS, TARGET_DB).deal(this.buffer);
    }

    @Override
    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return this.buffer;
    }

    private static class MeasurementsFetcher extends ConnectToMongo<Measurement> {
        public MeasurementsFetcher(String uri, String db) {
            super(uri, db);
        }

        @Override
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, buffer) -> {
                MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);
                Measurement m = buffer.poll();

                while (m != null) {
                    InsertOneResult res = collection.insertOne(m);
                    System.out.println("Inserted: " + res.getInsertedId());
                }
            });
        }
    }

    private static class MeasurementsPublisher extends ConnectToMongo<Measurement> {

        public MeasurementsPublisher(String uri, String db) {
            super(uri, db);
        }

        @Override
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, buffer) -> {
                MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);
                Measurement m = buffer.poll();

                while (m != null) {
                    InsertOneResult res = collection.insertOne(m);
                    System.out.println("Inserted: " + res.getInsertedId());
                }

            });
        }
    }
}