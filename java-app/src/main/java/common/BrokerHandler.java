package common;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class BrokerHandler<T> extends BrokerConnector {
    private final String topic;

    public BrokerHandler(String uri, String topic, int qos) throws MqttException {
        super(uri, topic, qos);
        this.topic = topic;
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

                onObjectArrived(getMappedObject(objectMapper, mqttMessage.toString()), topic);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
    }

    protected abstract T getMappedObject(ObjectMapper objectMapper, String message);

    protected abstract void onObjectArrived(T objectMapper, String topic);

    protected void publish(T m) throws MqttException {
        client.publish(this.topic, m.toString().getBytes(UTF_8), this.qos, false);
    }
}