package common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class BrokerFetcher<T> {
    private BrokerConnector connector;
    private LinkedBlockingQueue<T> buffer;
    private String topic;

    protected abstract Class<T> getMapperClass();

    public BrokerFetcher(BrokerConnector brokerConn, String topic, int qos) throws MqttException {
        this.connector = brokerConn;
        this.buffer = new LinkedBlockingQueue<>();
        this.topic = topic;

        try {
            connector.tryConnect();
            connector.getClient().subscribe(this.topic, qos);
            connector.getClient().setCallback(insertInBufferCallback());
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
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                System.out.println("Message arrived from broker (topic " + topic + "): " + mqttMessage);

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

                T object = objectMapper.readValue(mqttMessage.toString(), getMapperClass());
                getBuffer().put(object);
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
