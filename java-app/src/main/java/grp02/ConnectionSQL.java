package grp02;

import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

//Só para testes ainda!!!!

public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "t_sensores";// nome na especificação
    private static final int QOS = 0;

    public static void main(String[] args) throws MqttException {

        String[] collectionNames = {"sensort1"};

        ConnectToBroker subscriber = new ConnectToBroker(BROKER_URI);
        subscriber.connectAsSubsriber(TOPIC, QOS);

    }

}
