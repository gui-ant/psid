import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import common.BrokerSubscriber;
import common.IniConfig;
import grp07.Measurement;
import grp07.MongoHandler;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Esta classe não faz parte do projeto. Simula apenas as leituras dos sensores.
 * Os simuladores estão construídos apenas para mandar medições para um Broker.
 * Para gerar dados no broker, correr os SimulateSensor.jar nas pastas respetivas a cada Sensor (src/main/resources/)
 *
 * Esta classe, envia dados do broker para o cluster do Atlas (Cloud inicial), sendo que na apresentação esta Cloud será a incial deles.
 */

public class BrokerToMongo extends IniConfig {

    public BrokerToMongo(String iniFile) {
        super(iniFile);

        String mongoCloudUri = getConfig("mongo", "cloud_uri");
        String mongoCloudDb = getConfig("mongo", "cloud_db");

        String brokerUri = getConfig("broker", "uri");
        String brokerTopic = getConfig("broker", "topic_simul");
        int brokerQos = Integer.parseInt(getConfig("broker", "qos"));

        String[] collectionNames = getConfig("mongo","collections").split(",");

        try {

            MeasurementFetcher fetcher = new MeasurementFetcher(brokerUri, brokerTopic, brokerQos);
            MeasurementPublisher publisher = new MeasurementPublisher(mongoCloudUri, mongoCloudDb);

            for (String collection : collectionNames)
                fetcher.getBuffer().put(collection, new LinkedBlockingQueue<>());

            publisher.deal(fetcher.getBuffer());

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new BrokerToMongo("config.ini");
    }

    public static class MeasurementPublisher extends MongoHandler<Measurement> {

        public MeasurementPublisher(String sourceUri, String db) {
            super(sourceUri, db);
        }

        @Override
        protected void deal(HashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {
            collectionsDataBuffer.forEach((collectionName, collectionBuffer) -> {
                new Thread(() -> {
                    while (true) {
                        try {
                            Measurement m = collectionBuffer.take();

                            MongoCollection<Measurement> collection = getCollection(collectionName, Measurement.class);
                            collection.insertOne(m);

                            System.out.println("Published (Mongo):\t" + m);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            });

        }
    }

    public static class MeasurementFetcher extends BrokerSubscriber<Measurement> {

        private final HashMap<String, LinkedBlockingQueue<Measurement>> buffer = new HashMap<>();

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
                /*
                  Só acrescenta ao buffer, se já existir a key corresponente na HashMap, i.e. se se pretende considerar a leitura desse sensor.
                  Esta validação depende do parâmetro "collecionNames" passado no construtor
                 */
                String mongoCollectionName = "sensor" + object.getSensor().toLowerCase(Locale.ROOT);
                if (buffer.containsKey(mongoCollectionName)) {
                    buffer.get(mongoCollectionName).put(object);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        protected HashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
            return this.buffer;
        }
    }


}
