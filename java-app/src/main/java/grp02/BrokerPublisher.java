package grp02;

import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BrokerPublisher extends BrokerConnector {

    public BrokerPublisher(String URI, String topic, int qos) throws MqttException {
        super(URI, topic, qos);

        tryConnect();
    }

    public void startPublishing(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new ToBroker(client, topic, qos, buffer).start();
                }
        );
    }

    private static class ToBroker extends Thread {

        private final MqttClient client;
        private final String topic;
        private final int qos;
        private final LinkedBlockingQueue<Measurement> buffer;

        public ToBroker(MqttClient client, String topic, int qos, LinkedBlockingQueue<Measurement> buffer){
            this.client = client;
            this.buffer = buffer;
            this.topic = topic;
            this.qos = qos;
        }

        @Override
        public void run() {

            try {
                while(true) {

                    Measurement measurement = buffer.take();
                    publish(topic, measurement);

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
