package grp02;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

//Só para testes ainda!!!!

public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";

    public static void main(String[] args) {
        String[] collectionNames = {"sensort1"};

        try {

            MqttClient client = new MqttClient(
                    BROKER_URI,
                    MqttClient.generateClientId(),
                    new MemoryPersistence());

            System.out.println(client.getClientId());


            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println(topic + ": " + message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });

            client.connect();
            client.subscribe("t_sensores", 0);



        } catch (MqttException e){
            e.printStackTrace();
        }
    }

}
