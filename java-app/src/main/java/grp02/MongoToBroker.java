package grp02;

import com.mongodb.DBCursor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import common.BrokerConnector;
import common.BrokerPublisher;
import common.MongoConnector;
import common.MongoFetcher;
import grp07.Measurement;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.mongodb.client.model.Filters.eq;

public class MongoToBroker {

    private static final String TOPIC = "pisid_g07_sensors";
    private static final int QOS = 0;

    private final MongoConnector mongoConn;
    private final BrokerConnector brokerConn;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    MongoToBroker(MongoConnector mongoConn, BrokerConnector brokerConn, String[] collectionNames) throws MqttException {
        this.mongoConn = mongoConn;
        this.brokerConn = brokerConn;
        this.buffer = new ConcurrentHashMap<>();
        HashMap<String, MongoCollection<Measurement>> collections = new HashMap<>();

        for (String collectionName : collectionNames) {
            collections.put(collectionName, mongoConn.getCurrentDB().getCollection(collectionName, Measurement.class));
            if (!buffer.containsKey(collectionName))
                buffer.put(collectionName, new LinkedBlockingQueue<>());

            MeasurementFetcher fetcher = new MeasurementFetcher(collections);
            new MeasurementPublisher(brokerConn, TOPIC, QOS).startPublishing(fetcher.getCollectionBuffer(collectionName));
        }

    }

    private class MeasurementFetcher extends MongoFetcher<Measurement> {


        public MeasurementFetcher(HashMap<String, MongoCollection<Measurement>> collections) {
            super(collections);
        }

        @Override
        protected void startFetching() {
            new Thread(() -> {
                getCollections().forEach((name, collection) -> {
                    Measurement lastObj = getLastObject(collection);

                    MongoCursor<Measurement> cursor = collection.find(eq("_id", lastObj.getId())).iterator();
                    while (cursor.hasNext()) {
                        try {
                            Measurement m = cursor.next();
                            getBuffer().get(name).put(m);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            cursor.close();
                        }
                    }
                });

            }).start();
        }

        @Override
        protected Measurement getLastObject(MongoCollection<Measurement> collection) {
            return collection.find().sort(new Document("_id", -1)).limit(1).first();
        }
    }

    private class MeasurementPublisher extends BrokerPublisher<Measurement> {

        public MeasurementPublisher(BrokerConnector brokerConn, String topic, int qos) {
            super(brokerConn, topic, qos);
        }

        @Override
        protected byte[] getPayload(Measurement m) {
            return m.toByteArray();
        }
    }

    public static void main(String[] args) throws MqttException {
        String SOURCE_URI = "mongodb://127.0.0.1:27017/g07";
        String SOURCE_DB = "g07";
        String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";

        String[] collectionNames = {"sensort1","sensort2"};

        MongoConnector cluster = new MongoConnector(SOURCE_URI);
        cluster.useDatabase(SOURCE_DB);

        BrokerConnector broker = new BrokerConnector(BROKER_URI);
        new MongoToBroker(cluster, broker, collectionNames);
    }
}
