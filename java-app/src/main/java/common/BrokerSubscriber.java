package common;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class BrokerSubscriber<T> extends BrokerConnector {

    public BrokerSubscriber(String uri, String topic, int qos) throws MqttException {
        super(uri, topic, qos);
        try {
            super.tryConnect();
            super.getClient().setCallback(insertInBufferCallback());
            super.getClient().subscribe(topic, qos);
            System.err.println("URI: " + uri + "; TOPICO: " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttCallback insertInBufferCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.err.println("connectionLost");
                return;
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                System.err.println("BROKER SUBSCRIBER: messageArrived");
                T obj = getMappedObject(mqttMessage.toString());
                onObjectArrived(obj, topic);
                System.out.println("Fetched (Broker):\t" + obj);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                return;
            }
        };
    }

    protected abstract T getMappedObject(String message);

    protected abstract void onObjectArrived(T objectMapper, String topic);
}
