package grp02;

import com.mongodb.client.MongoCollection;
import common.BrokerConnector;
import common.BrokerPublisher;
import common.MongoConnector;
import common.MongoFetcher;
import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MongoToBroker {

    private static final String TOPIC_PREFIX = "";
    private final MongoConnector mongoConn;
    private final BrokerConnector brokerConn;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    MongoToBroker(MongoConnector mongoConn, BrokerConnector brokerConn, String[] collectionNames) throws MqttException {
        this.mongoConn = mongoConn;
        this.brokerConn = brokerConn;
        for (String collectionName : collectionNames) {
            if (!buffer.containsKey(collectionName))
                buffer.put(collectionName, new LinkedBlockingQueue<>());

            MongoCollection<Measurement> collection = mongoConn.getCurrentDB().getCollection(collectionName, Measurement.class);
            MeasurementFetcher fetcher = new MeasurementFetcher(collection, buffer.get(collectionName));
            fetcher.start();
            new MeasurementPublisher(brokerConn, TOPIC_PREFIX + collectionName, 0).startPublishing(fetcher.getBuffer());
        }
    }

    private class MeasurementFetcher extends MongoFetcher<Measurement> {

        public MeasurementFetcher(MongoCollection<Measurement> collection, LinkedBlockingQueue<Measurement> buffer) {
            super(collection, buffer);
        }

        @Override
        protected Class<Measurement> getMapperClass() {
            return Measurement.class;
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
        String SOURCE_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
        String SOURCE_DB = "g07";
        String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
        String TOPIC_PREFIX = "pisid_g07_";// nome na especificação
        int QOS = 0;
        String[] collectionNames = {"sensort1"};

        MongoConnector cluster = new MongoConnector(SOURCE_URI);
        cluster.useDatabase(SOURCE_DB);

        BrokerConnector broker = new BrokerConnector(BROKER_URI);
        new MongoToBroker(cluster, broker, collectionNames);
    }
}
