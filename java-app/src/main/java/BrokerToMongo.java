import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;

import common.BrokerHandler;
import grp07.MongoHandler;
import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Esta classe não faz parte do projeto. Simula apenas as leituras dos sensores.
 * Os simuladores estão construídos apenas para mandar medições para um Broker.
 * Para gerar dados no broker, correr os SimulateSensor.jar nas pastas respetivas a cada Sensor (src/main/resources/)
 *
 * Esta classe, envia dados do broker para o cluster do Atlas (Cloud inicial), sendo que na apresentação esta Cloud será a incial deles.
 */

public class BrokerToMongo {
    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "pisid_g07_simulators";

    private static final String TARGET_URI = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String TARGET_DB = "g07";

    private static final int QOS = 0;

    public static void main(String[] args) {

        String[] collectionNames = {"sensort1", "sensort2"};

        try {

            MeasurementFetcher fetcher = new MeasurementFetcher(BROKER_URI, TOPIC, QOS);
            MeasurementPublisher publisher = new MeasurementPublisher(TARGET_URI, TARGET_DB);

            for (String collection : collectionNames)
                fetcher.getBuffer().put(collection, new LinkedBlockingQueue<>());

            publisher.deal(fetcher.getBuffer());

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static class MeasurementPublisher extends MongoHandler<Measurement> {

        public MeasurementPublisher(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, collectionBuffer) -> {
                new Thread(() -> {
                    while (true) {
                        try {
                            Measurement m = collectionBuffer.take();

                            MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);
                            InsertOneResult res = collection.insertOne(m);

                            System.out.println("Published: " + m);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            });

        }
    }

    public static class MeasurementFetcher extends BrokerHandler<Measurement> {

        private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer = new ConcurrentHashMap<>();

        public MeasurementFetcher(String uri, String topic, int qos) throws MqttException {
            super(uri, topic, qos);
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
            try {
                /**
                 * Só acrescenta ao buffer, se já existir a key corresponente na HashMap, i.e. se se pretende considerar a leitura desse sensor.
                 * Esta validação depende do parâmetro "collecionNames" passado no construtor
                 */
                String mongoCollectionName = "sensor" + object.getSensor().toLowerCase(Locale.ROOT);
                if (buffer.containsKey(mongoCollectionName)) {
                    buffer.get(mongoCollectionName).put(object);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        protected ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
            return this.buffer;
        }
    }


}
