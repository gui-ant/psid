import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import common.*;
import grp07.Measurement;
import grp07.MongoHandler;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MethodsComparison extends IniConfig {
    private MeasMongoFetcher measMongoFetcher;
    private MeasBrokerPublisher measBrokerPublisher;
    private MeasBrokerFetcher measBrokerFetcher;
    private MeasMongoPublisher measMongoPublisher;

    private static final String MONGO_COLLECTION_NAME = "sensort1";

    private HashMap<String, LinkedBlockingQueue<Measurement>> buffer = new HashMap<>();
    private int counter;

    public MethodsComparison(String iniFile, MigrationMethod m, int counter) {
        super(iniFile);
        this.buffer.put(MONGO_COLLECTION_NAME, new LinkedBlockingQueue<>());
        this.counter = counter;

        long start = System.currentTimeMillis();


        MongoConnector mc = new MongoConnector(getConfig("mongo", "local_uri"), getConfig("mongo", "local_db"));
        MongoCollection<Measurement> collection = mc.getCurrentDb().getCollection(MONGO_COLLECTION_NAME, Measurement.class);
        BasicDBObject d = new BasicDBObject();
        collection.deleteMany(d);

        switch (m) {
            case DIRECT:
                testDirect();
                break;
            case MQTT:
                testMQTT();
                break;
        }

        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println(m + ": " + elapsedTime + " msec. to migrate " + mc.getCurrentDb().getCollection(MONGO_COLLECTION_NAME).countDocuments() + " records.");
    }

    public static void main(String[] args) throws MqttException {
        MigrationMethod m = MigrationMethod.MQTT;
        int counter = 100;
        new MethodsComparison("config.ini", m, counter);
    }

    private void testDirect() {
        this.measMongoFetcher = new MeasMongoFetcher(getConfig("mongo", "cloud_uri"), "g07") {
            @Override
            void fetch(Measurement m) {
                try {
                    buffer.get(MONGO_COLLECTION_NAME).put(m);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        this.measMongoFetcher.deal(this.buffer);
        this.measMongoPublisher = new MeasMongoPublisher(getConfig("mongo", "local_uri"), "g07") {
            @Override
            protected Measurement getMeasurement() {
                return buffer.get(MONGO_COLLECTION_NAME).poll();
            }
        };
        this.measMongoPublisher.deal(this.buffer);
    }

    private synchronized void testMQTT() {
        String brokerUri = getConfig("broker", "uri");
        String brokerTopic = getConfig("broker", "topic_tests");
        int qos = Integer.valueOf(getConfig("broker", "qos"));


        this.measMongoFetcher = new MeasMongoFetcher(getConfig("mongo", "cloud_uri"), getConfig("mongo", "cloud_db")) {
            @Override
            void fetch(Measurement m) {
                try {
                    buffer.get(MONGO_COLLECTION_NAME).put(m);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        this.measMongoFetcher.deal(this.buffer);
        try {
            this.measBrokerPublisher = new MeasBrokerPublisher(brokerUri, brokerTopic, qos);
            this.measBrokerPublisher.startPublishing(this.buffer);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        try {
            this.measBrokerFetcher = new MeasBrokerFetcher(brokerUri, brokerTopic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    class MeasBrokerFetcher extends BrokerSubscriber<Measurement> {
        public MeasBrokerFetcher(String uri, String topic, int qos) throws MqttException {
            super(uri, topic, qos);
            System.err.println("DEPOIS DO SUPER");

        }

        @Override
        protected Measurement getMappedObject(String message) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                return objectMapper.readValue(message, Measurement.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onObjectArrived(Measurement object, String topic) {
            measMongoPublisher.getCurrentDb().getCollection(MONGO_COLLECTION_NAME, Measurement.class).insertOne(object);
            System.err.println("AQUI!!!");
        }
    }

    private class MeasBrokerPublisher extends BrokerPublisher<Measurement> {
        public MeasBrokerPublisher(String URI, String topic, int qos) throws MqttException {
            super(URI, topic, qos);
        }

        @Override
        public void startPublishing(HashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
            try {
                while (sourceBuffer.get(MONGO_COLLECTION_NAME).peek() != null)
                    getClient().publish("pisid_g07_tests", sourceBuffer.get(MONGO_COLLECTION_NAME).take().toString().getBytes(StandardCharsets.UTF_8), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private abstract class MeasMongoFetcher extends MongoHandler<Measurement> {
        public MeasMongoFetcher(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            MongoCollection<Measurement> collection = getCollection(MONGO_COLLECTION_NAME, Measurement.class);
            MongoCursor<Measurement> cursor = collection.find().iterator();
            new Thread(() -> {
                while (counter-- > 0)
                    fetch(cursor.next());
            }).start();

        }

        abstract void fetch(Measurement m);
    }

    private abstract class MeasMongoPublisher extends MongoHandler<Measurement> {
        public MeasMongoPublisher(String sourceUri, String db) {
            super(sourceUri, db);
            MongoCollection<Measurement> collection = getCollection(MONGO_COLLECTION_NAME, Measurement.class);
            BasicDBObject d = new BasicDBObject();
            collection.deleteMany(d);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            MongoCollection<Measurement> collection = getCollection(MONGO_COLLECTION_NAME, Measurement.class);
            Measurement m;
            while ((m = getMeasurement()) != null)
                collection.insertOne(m);

        }

        protected abstract Measurement getMeasurement();
    }
}
