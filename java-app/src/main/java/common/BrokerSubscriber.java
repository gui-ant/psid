package common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class BrokerSubscriber<T> extends BrokerConnector {

    LinkedBlockingQueue<T> buffer;

    public BrokerSubscriber(String URI, String topic, int qos) throws MqttException {
        super(URI, topic, qos);
        buffer = new LinkedBlockingQueue<>();

        client.setCallback(insertInBufferCallback());
        tryConnect();
        client.subscribe(topic, this.qos);
    }

    public LinkedBlockingQueue<T> getBuffer() {
        return buffer;
    }

    private MqttCallback insertInBufferCallback() {
        return new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("Connection lost!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                System.out.println(BrokerSubscriber.this.topic + ": Message arrived from broker (topic " + topic + "): " + message);

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

                T object;
                try {
                    object = objectMapper.readValue(message.toString(), getMapperClass());
                    getBuffer().put(object);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        };
    }

    protected abstract Class<T> getMapperClass();
}
