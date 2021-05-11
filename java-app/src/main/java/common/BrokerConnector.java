package common;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class BrokerConnector {

    private MqttClient client;

    public BrokerConnector(String uri) {
        try {
            this.client = new MqttClient(
                    uri,
                    MqttClient.generateClientId(),
                    new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void tryConnect() throws MqttException {
        if (!client.isConnected())
            client.connect();
        System.out.println("Client " + client.getClientId() + " connected successfully to broker!");
    }

    public MqttClient getClient() {
        return client;
    }
}
