package common;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class BrokerPublisher<T> {
    private static BrokerConnector connector;
    private static final String TOPIC_PREFIX = "pisid_g07_";
    private LinkedBlockingQueue<T> buffer;
    private String topic;
    private int qos;

    public BrokerPublisher(BrokerConnector brokerConn, String topic, int qos) {
        this.connector = brokerConn;
        this.buffer = new LinkedBlockingQueue<>();
        this.topic = topic;
        this.qos = qos;
        try {
            brokerConn.tryConnect();
            brokerConn.getClient().subscribe(this.topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void startPublishing(LinkedBlockingQueue<T> buffer) throws MqttException {
        while (true) {
            T obj = null;
            try {
                obj = getBuffer().take();
                connector.getClient().publish(topic, getPayload(obj), this.qos, false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    protected abstract byte[] getPayload(T obj);

    public LinkedBlockingQueue<T> getBuffer() {
        return buffer;
    }
}
