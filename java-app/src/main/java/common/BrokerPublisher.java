package common;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BrokerPublisher<T> extends BrokerConnector {

    public BrokerPublisher(String URI, String topic, int qos) throws MqttException {
        super(URI, topic, qos);

        tryConnect();
    }

    public void startPublishing(HashMap<String, LinkedBlockingQueue<T>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new ToBroker(getClient(), getTopic(), getQos(), buffer).start();
                }
        );
    }

    private class ToBroker extends Thread {

        private final MqttClient client;
        private final String topic;
        private final int qos;
        private final LinkedBlockingQueue<T> buffer;

        public ToBroker(MqttClient client, String topic, int qos, LinkedBlockingQueue<T> buffer) {
            this.client = client;
            this.buffer = buffer;
            this.topic = topic;
            this.qos = qos;
        }

        @Override
        public void run() {

            try {
                while (true) {
                    T obj = buffer.take();
                    publish(topic, obj);

                }

            } catch (InterruptedException | MqttException e) {
                e.printStackTrace();
            }
        }

        private void publish(String topic, T object) throws MqttException {
            client.publish(topic,
                    object.toString().getBytes(UTF_8), // does it work tho?
                    qos,
                    false);
        }
    }

}