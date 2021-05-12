package common;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class BrokerFetcher<T> extends BrokerConnector {
    private final LinkedBlockingQueue<T> buffer;
    private final String topic;

    protected abstract Class<T> getMapperClass();

    public BrokerFetcher(String uri, String topic, int qos) throws MqttException {
        super(uri, topic, qos);
        this.topic = topic;
        this.buffer = new LinkedBlockingQueue<>();

        try {
            super.tryConnect();
            super.getClient().subscribe(this.topic, qos);
            super.getClient().setCallback(insertInBufferCallback());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttCallback insertInBufferCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {

                System.out.println("Message arrived from broker (topic " + topic + "): " + mqttMessage);

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

                T object = null;
                try {
                    object = objectMapper.readValue(mqttMessage.toString(), getMapperClass());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                try {
                    getBuffer().put(object);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
    }


    public LinkedBlockingQueue<T> getBuffer() {
        return buffer;
    }
}