package grp02;

import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ConnectToBroker {

    private static final String TOPIC = "t_sensores";

    private final MqttClient client;

    public ConnectToBroker(String URI) throws MqttException {
        this.client = new MqttClient(
                URI,
                MqttClient.generateClientId(),
                new MemoryPersistence());

        System.out.println(client.getClientId());

        this.client.connect();
    }

    public void startPublishing(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new BrokerPublisher(client, buffer).start();
                }
        );
    }

    static class BrokerPublisher extends Thread {

        private final LinkedBlockingQueue<Measurement> buffer;
        private final MqttClient client;

        public BrokerPublisher(MqttClient client, LinkedBlockingQueue<Measurement> buffer){
            this.client = client;
            this.buffer = buffer;
        }

        @Override
        public void run() {

            try {
                while(true) {

                    Measurement measurement = buffer.take();
                    publish(measurement);

                }

            } catch (InterruptedException | MqttException e) {
                e.printStackTrace();
            }
        }

        private void publish(Measurement m) throws MqttException {
            client.publish(
                    TOPIC,
                    m.toString().getBytes(UTF_8), // does it work tho?
                    0, // QoS
                    false);
        }
    }
}
