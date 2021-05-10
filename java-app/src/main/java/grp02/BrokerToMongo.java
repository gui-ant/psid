package grp02;

import com.mongodb.client.MongoCollection;
import common.BrokerConnector;
import common.BrokerFetcher;
import common.MongoConnector;
import common.MongoPublisher;
import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class BrokerToMongo {
    private static final String TOPIC_PREFIX = "pisid_g07_";

    private BrokerFetcher<Measurement> measurementFetcher = null;
    private MongoPublisher<Measurement> measurementPublisher = null;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public BrokerToMongo(BrokerConnector brokerConn, MongoConnector mongoConn, String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();

        for (String collection : collectionNames) {
            if (!buffer.containsKey(collection))
                buffer.put(collection, new LinkedBlockingQueue<>());

            try {
                System.out.println(TOPIC_PREFIX + collection);
                //measurementFetcher = new MeasurementFetcher(uri, TOPIC_PREFIX + collection, 0);
                measurementFetcher = new MeasurementFetcher(brokerConn, TOPIC_PREFIX + collection, 0);
                measurementPublisher = new MeasurementPublisher(
                        mongoConn.getCurrentDB().getCollection(collection, Measurement.class),
                        measurementFetcher.getBuffer()
                );
                measurementPublisher.start();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public class MeasurementPublisher extends MongoPublisher<Measurement> {

        public MeasurementPublisher(MongoCollection<Measurement> collection, LinkedBlockingQueue<Measurement> buffer) {
            super(collection, buffer);
        }
    }

    public class MeasurementFetcher extends BrokerFetcher<Measurement> {

        public MeasurementFetcher(BrokerConnector brokerConn, String topic, int qos) throws MqttException {
            super(brokerConn, topic, qos);
        }

        @Override
        protected Class<Measurement> getMapperClass() {
            return Measurement.class;
        }
    }

    public static void main(String[] args) {
        BrokerConnector b = new BrokerConnector("tcp://broker.mqttdashboard.com:1883");
        MongoConnector m = new MongoConnector("mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority");

        m.useDatabase("g07");
        String[] collectionNames = {"sensort1", "sensort2"};

        new BrokerToMongo(b, m, collectionNames);

    }
}
