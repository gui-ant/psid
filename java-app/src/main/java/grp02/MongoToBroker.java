package grp02;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import common.BrokerPublisher;
import common.IniConfig;
import grp07.Measurement;
import grp07.MongoHandler;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MongoToBroker extends IniConfig {

    private HashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public MongoToBroker(String iniFile) {
        super(iniFile);
        String mongoLocalUri = getConfig("mongo", "local_uri");
        String mongoLocalDb = getConfig("mongo", "local_db");
        String brokerUri = getConfig("broker", "uri");
        String brokerTopic = getConfig("broker", "topic");
        int brokerQos = Integer.parseInt(getConfig("broker", "qos"));

        int sleepTime = Integer.parseInt(getConfig("cluster_to_mysql", "sleep_time"));
        ;
        String[] collectionNames = getConfig("mongo", "collections").split(",");

        this.buffer = new HashMap<>();
        for (String collection : collectionNames)
            this.buffer.put(collection, new LinkedBlockingQueue<>());

        ConnectToMongo cluster = new ConnectToMongo(mongoLocalUri, mongoLocalDb, sleepTime);
        cluster.useCollections(collectionNames);
        cluster.deal(this.buffer);

        try {
            new BrokerPublisher<Measurement>(brokerUri, brokerTopic, brokerQos).startPublishing(this.buffer);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MqttException {
        new MongoToBroker("config.ini");
    }

    static class ConnectToMongo extends MongoHandler<Measurement> {
        private final int sleepTime;

        public ConnectToMongo(String sourceUri, String db, int sleepTime) {
            super(sourceUri, db);
            this.sleepTime = sleepTime;
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collection, buffer) ->
                    new MeasureFetcher(getCollection(collection, Measurement.class), buffer).start()
            );
        }

        class MeasureFetcher extends Thread {

            private final MongoCollection<Measurement> collection;
            private final LinkedBlockingQueue<Measurement> buffer;

            public MeasureFetcher(MongoCollection<Measurement> collection, LinkedBlockingQueue<Measurement> buffer) {
                this.collection = collection;
                this.buffer = buffer;
            }

            @Override
            public void run() {

                Measurement doc = getLastObject(this.collection);
                // TODO: Considerar a collection estar vazia,i.e. gerar doc = null
                ObjectId lastId = doc.getId();
                while (true) {
                    try {
                        System.out.println("Fetching " + getCollectionName(collection) + "...");

                        MongoCursor<Measurement> cursor = collection.find(Filters.gt("_id", lastId)).iterator();

                        // Le os novos dados e adiciona-os ao buffer
                        while (cursor.hasNext()) {
                            doc = cursor.next();
                            lastId = doc.getId();
                            buffer.offer(doc);
                            System.out.println("Fetched (Mongo):\t" + doc);
                        }

                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
