package common;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public abstract class BrokerConnector {

    private final MqttClient client;
    private final String topic;
    private final int qos;

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
        System.out.println("Connection stablished with broker! (client:" + client.getClientId() + ")");
    }

    public MqttClient getClient() {
        return client;
    }

    public String getTopic() {
        return topic;
    }

    public int getQos() {
        return qos;
    }
}
