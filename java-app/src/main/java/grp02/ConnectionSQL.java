package grp02;

import org.eclipse.paho.client.mqttv3.*;

//Só para testes ainda!!!!

public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "t_sensores";// nome na especificação
    private static final int QOS = 0;

    public static void main(String[] args) throws MqttException {

        ConnectToBroker subscriber = new ConnectToBroker(BROKER_URI);
        subscriber.connectAsSubscriber(TOPIC, QOS);

    }

}
