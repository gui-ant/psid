import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import common.BrokerPublisher;
import common.BrokerSubscriber;
import common.IniConfig;
import common.MigrationMethod;
import grp07.Measurement;
import grp07.MongoHandler;
import org.eclipse.paho.client.mqttv3.MqttException;

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
        Thread a = new Thread(() -> {
            switch (m) {
                case DIRECT:
                    testDirect();
                    break;
                case MQTT:
                    testMQTT();
                    break;
            }
        });
        a.start();
        synchronized (a) {
            try {
                a.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            long elapsedTime = end - start;
            System.out.println(m + ": " + elapsedTime + " msec. to migrate " + counter + " records.");
        }

    }

    public static void main(String[] args) throws MqttException {
        MigrationMethod m = MigrationMethod.MQTT;
        int counter = 100;
        MethodsComparison p = new MethodsComparison("config.ini", m, counter);
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


        new MeasMongoFetcher(getConfig("mongo", "cloud_uri"), getConfig("mongo", "cloud_db")) {
            @Override
            void fetch(Measurement m) {
                try {
                    buffer.get(MONGO_COLLECTION_NAME).put(m);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

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
        }

        @Override
        protected void onObjectArrived(Measurement object, String topic) {
            measMongoPublisher.getCurrentDb().getCollection(MONGO_COLLECTION_NAME, Measurement.class).insertOne(object);
        }
    }

    private class MeasBrokerPublisher extends BrokerPublisher<Measurement> {
        public MeasBrokerPublisher(String URI, String topic, int qos) throws MqttException {
            super(URI, topic, qos);
        }
    }

    private abstract class MeasMongoFetcher extends MongoHandler<Measurement> {
        public MeasMongoFetcher(String sourceUri, String db) {
            super(sourceUri, db);
            deal(MethodsComparison.this.buffer);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            MongoCollection<Measurement> collection = getCollection(MONGO_COLLECTION_NAME, Measurement.class);
            MongoCursor<Measurement> cursor = collection.find().iterator();
            new Thread(() -> {
                synchronized (this) {
                    while (counter-- > 0)
                        fetch(cursor.next());
                    notify();
                }
            }).start();
        }

        abstract void fetch(Measurement m);
    }

    private abstract class MeasMongoPublisher extends MongoHandler<Measurement> {
        public MeasMongoPublisher(String sourceUri, String db) {
            super(sourceUri, db);
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
