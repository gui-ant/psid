package grp02;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public abstract class BrokerConnector {

    protected final MqttClient client;
    protected final String topic;
    protected final int qos;

    public BrokerConnector(String URI, String topic, int qos) throws MqttException {
        this.client = new MqttClient(
                URI,
                MqttClient.generateClientId(),
                new MemoryPersistence());

        this.topic = topic;
        this.qos = qos;
    }

    protected void tryConnect() throws MqttException {
        client.connect();
        System.out.println(client.getClientId());
        System.out.println("Connected successfully");
    }

    public MqttClient getClient() {
        return client;
    }
}
