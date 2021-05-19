package grp02;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import common.BrokerHandler;
import common.IniConfig;
import grp07.Measurement;
import grp07.MongoHandler;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MongoToBroker extends IniConfig {

    private HashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public MongoToBroker(String iniFile) {
        super(iniFile);
        String mongoLocalUri = getConfig("mongo", "local_uri");
        String mongoLocalDb = getConfig("mongo", "local_db");
        String brokerUri = getConfig("broker", "uri");
        String brokerTopic = getConfig("broker", "topic");
        int brokerQos = Integer.parseInt(getConfig("broker", "qos"));

        String[] collectionNames = getConfig("mongo", "collections").split(",");

        this.buffer = new HashMap<>();
        for (String collection : collectionNames)
            this.buffer.put(collection, new LinkedBlockingQueue<>());

        ConnectToMongo cluster = new ConnectToMongo(mongoLocalUri, mongoLocalDb);
        cluster.useCollections(collectionNames);
        cluster.deal(this.buffer);

        try {
            new BrokerPublisher(brokerUri, brokerTopic, brokerQos).startPublishing(this.buffer);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MqttException {
        new MongoToBroker("config.ini");
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


        public void startPublishing(HashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
            sourceBuffer.forEach(
                    (collectionName, buffer) -> new ToBroker(getClient(), getTopic(), getQos(), buffer).start()
            );
        }

        private class ToBroker extends Thread {

            private final MqttClient client;
            private final String topic;
            private final int qos;
            private LinkedBlockingQueue<Measurement> buffer;

            public ToBroker(MqttClient client, String topic, int qos, LinkedBlockingQueue<Measurement> buffer) {
                this.client = client;
                this.buffer = buffer;
                this.topic = topic;
                this.qos = qos;
            }

            @Override
            public void run() {
                Measurement obj;
                while ((obj = buffer.poll()) != null) {
                    try {
                        publish(topic, obj);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
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
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collection, buffer) ->
                    new MeasureFetcher(getCollection(collection, Measurement.class), buffer).start()
            );
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
                            System.out.println("Fetched:\t" + doc);
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
