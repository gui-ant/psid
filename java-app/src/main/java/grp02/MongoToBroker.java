package grp02;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import common.BrokerHandler;
import grp07.Measurement;
import grp07.MongoHandler;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MongoToBroker {

    private static final String MONGO_LOCAL_URI = "mongodb://127.0.0.1:27017";
    private static final String MONGO_LOCAL_DB = "g07";

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String BROKER_TOPIC = "pisid_g07_sensors";
    private static final int BROKER_QOS = 0;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public MongoToBroker(String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        for (String collection : collectionNames)
            this.buffer.put(collection, new LinkedBlockingQueue<>());

        ConnectToMongo cluster = new ConnectToMongo(MONGO_LOCAL_URI, MONGO_LOCAL_DB);
        cluster.useCollections(collectionNames);
        cluster.deal(this.buffer);

        try {
            new BrokerPublisher(BROKER_URI, BROKER_TOPIC, BROKER_QOS).startPublishing(cluster.getFetchingSource());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MqttException {

        String[] collectionNames = {"sensort1"};

        new MongoToBroker(collectionNames);
    }

    public class BrokerPublisher extends BrokerHandler<Measurement> {


        public BrokerPublisher(String uri, String topic, int qos) throws MqttException {
            super(uri, topic, qos);
        }

        @Override
        protected Measurement getMappedObject(String message) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

            try {
                return objectMapper.readValue(message, Measurement.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onObjectArrived(Measurement m, String topic) {
            try {
                buffer.get(topic).put(m);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        public void
        startPublishing(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
            sourceBuffer.forEach(
                    (collectionName, buffer) -> new ToBroker(client, BROKER_TOPIC, qos, buffer).start()
            );
        }

        private class ToBroker extends Thread {

            private final MqttClient client;
            private final String topic;
            private final int qos;
            private final LinkedBlockingQueue<Measurement> buffer;

            public ToBroker(MqttClient client, String topic, int qos, LinkedBlockingQueue<Measurement> buffer) {
                this.client = client;
                this.buffer = buffer;
                this.topic = topic;
                this.qos = qos;
            }

            @Override
            public void run() {

                try {
                    while (true) {

                        Measurement obj = buffer.take();
                        publish(topic, obj);

                    }

                } catch (InterruptedException | MqttException e) {
                    e.printStackTrace();
                }
            }

            private void publish(String topic, Measurement m) throws MqttException {
                client.publish(topic,
                        m.toString().getBytes(UTF_8), // does it work tho?
                        qos,
                        false);
            }
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
