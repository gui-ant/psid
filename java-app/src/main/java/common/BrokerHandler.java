package common;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class BrokerHandler<T> extends BrokerConnector {

    public BrokerHandler(String uri, String topic, int qos) throws MqttException {
        super(uri, topic, qos);
        try {
            super.tryConnect();
            super.getClient().subscribe(topic, qos);
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
                T obj = getMappedObject(mqttMessage.toString());
                onObjectArrived(obj, topic);
                System.out.println("Fetched:\t" + obj);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
    }

    protected abstract T getMappedObject(String message);

    protected abstract void onObjectArrived(T objectMapper, String topic);

    protected void publish(T m) throws MqttException {
        getClient().publish(getTopic(), m.toString().getBytes(UTF_8), getQos(), false);
    }
}
