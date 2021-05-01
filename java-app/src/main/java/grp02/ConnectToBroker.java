package grp02;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import grp07.Measurement;
import org.bson.BsonBinary;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ConnectToBroker {

    private final MqttClient client;
    private String topic;

    public ConnectToBroker(String URI) throws MqttException {
        this.client = new MqttClient(
                URI,
                MqttClient.generateClientId(),
                new MemoryPersistence());
    }

    public void connectAsPublisher(String topic) throws MqttException {
        this.topic = topic;
        tryConnect();
    }

    public void connectAsSubscriber(String topic, int QOS) throws MqttException {
        this.topic = topic;

        client.setCallback(sqlCallback());

        tryConnect();
        client.subscribe(topic, QOS);
    }

    private void tryConnect() throws MqttException {
        client.connect();
        System.out.println(client.getClientId());
        System.out.println("Connected successfully");
    }

    private MqttCallback sqlCallback(){
        return new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) { System.err.println("Connection lost!"); }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                String[] message_info = MyUtils.messageIntoArray(message);
                for (String s: message_info) { System.out.print(s); }

                try {

                    Measurement measurement = MyUtils.buildMeasurement(message_info);

                } catch (IllegalArgumentException e) {
                    System.err.println("Illegal argument: Array size was incorrect");
                }



            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        };
    }

    public void startPublishing(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new BrokerPublisher(client, topic, buffer).start();
                }
        );
    }

    static class BrokerPublisher extends Thread {

        private final MqttClient client;
        private final String topic;
        private final LinkedBlockingQueue<Measurement> buffer;

        public BrokerPublisher(MqttClient client, String topic, LinkedBlockingQueue<Measurement> buffer){
            this.client = client;
            this.buffer = buffer;
            this.topic = topic;
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
            client.publish(
                    topic,
                    m.toString().getBytes(UTF_8), // does it work tho?
                    0, // QoS
                    false);
        }
    }
}
