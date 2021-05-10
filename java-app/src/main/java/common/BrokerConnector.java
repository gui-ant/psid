package common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


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
        System.out.println(client.getClientId());
        System.out.println("Connected successfully");
    }

    public MqttClient getClient() {
        return client;
    }
}
