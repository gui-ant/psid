import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;

import common.BrokerFetcher;
import common.MongoConnector;
import common.MongoPublisher;
import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Envia dados do broker para o cluster do Atlas (Cloud inicial)
 * Para gerar dados no broker, correr os SimulateSensor.jar nas pastas respetivas a cada Sensor (src/main/resources/)
 */

public class BrokerToMongo {
    private static final String TARGET_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String TARGET_DB = "g07";

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "pisid_g07_sensors";
    private static final int QOS = 0;

    private BrokerFetcher<Measurement> measurementFetcher = null;
    private MongoPublisher<Measurement> measurementPublisher = null;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public BrokerToMongo(String brokerUri, MongoConnector mongoConn, String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        HashMap<String, MongoCollection<Measurement>> collections = new HashMap<>();

        for (String collection : collectionNames) {
            collections.put(collection, mongoConn.getCurrentDb().getCollection(collection, Measurement.class));
            buffer.put(collection, new LinkedBlockingQueue<>());
        }

        try {
            measurementFetcher = new MeasurementFetcher(brokerUri, TOPIC, QOS);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        measurementPublisher = new MeasurementPublisher(collections, measurementFetcher.getBuffer());
        measurementPublisher.startPublishing();
    }

    public class MeasurementPublisher extends MongoPublisher<Measurement> {
        private LinkedBlockingQueue<Measurement> buffer;

        public MeasurementPublisher(HashMap<String, MongoCollection<Measurement>> collections, LinkedBlockingQueue<Measurement> buffer) {
            super(collections, new ConcurrentHashMap<>());
            this.buffer = buffer;
        }

        @Override
        public void startPublishing() {
            new Thread(() -> {
                while (true) {
                    try {
                        Measurement m = buffer.take();

                        String collectionName = "sensor" + m.getSensor().toLowerCase(Locale.ROOT);
                        MongoCollection<Measurement> collection = getCollections().get(collectionName);

                        InsertOneResult res = collection.insertOne(m);

                        System.out.println("Published: " + m);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    public class MeasurementFetcher extends BrokerFetcher<Measurement> {

        public MeasurementFetcher(String uri, String topic, int qos) throws MqttException {
            super(uri, topic, qos);
        }

        @Override
        protected Class<Measurement> getMapperClass() {
            return Measurement.class;
        }
    }

    public static void main(String[] args) {
        MongoConnector m = new MongoConnector(TARGET_URI);
        m.useDatabase(TARGET_DB);

        String[] collectionNames = {"sensort1", "sensort2"};

        new BrokerToMongo(BROKER_URI, m, collectionNames);

    }
}