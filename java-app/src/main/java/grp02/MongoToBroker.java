package grp02;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import common.BrokerPublisher;
import grp07.Measurement;
import grp07.MongoHandler;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MongoToBroker {

    private static final String SOURCE_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "pisid_g07_sensors_cenas";// nome na especificação
    private static final int QOS = 0;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public MongoToBroker(String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        for (String collection : collectionNames)
            this.buffer.put(collection, new LinkedBlockingQueue<>());

        ConnectToMongo cluster = new ConnectToMongo(SOURCE_URI_ATLAS, SOURCE_DB);
        cluster.useCollections(collectionNames);
        cluster.deal(this.buffer);

        try {
            new BrokerPublisher(BROKER_URI, TOPIC, QOS).startPublishing(cluster.getFetchingSource());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MqttException {

        String[] collectionNames = {"sensort1"};

        new MongoToBroker(collectionNames);
    }

    public void startPublishing(String brokerUri, String topic, int qos) {
        try {
            new BrokerPublisher<Measurement>(brokerUri, topic, qos).startPublishing(this.buffer);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    static class ConnectToMongo extends MongoHandler<Measurement> {

        public ConnectToMongo(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            new Thread(() -> {
                collectionsDataBuffer.forEach((collection, buffer) -> {
                    new MeasureFetcher(getCollection(collection, Measurement.class), buffer);
                });
            }).start();
        }

        class MeasureFetcher extends Thread {

            private final MongoCollection<Measurement> collection;
            private final LinkedBlockingQueue<Measurement> buffer;
            private static final int SLEEP_TIME = 5000;

            public MeasureFetcher(MongoCollection<Measurement> collection, LinkedBlockingQueue<Measurement> buffer) {
                this.collection = collection;
                this.buffer = buffer;
            }

            @Override
            public void run() {

                Measurement doc = getLastObject(collection);

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
                            System.out.println("Fetched: " + doc.getId());
                        }
                        sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
