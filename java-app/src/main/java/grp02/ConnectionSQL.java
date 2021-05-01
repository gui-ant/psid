package grp02;

import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.LinkedBlockingQueue;

//Só para testes ainda!!!!

public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "t_sensores";// nome na especificação
    private static final int QOS = 0;

    private LinkedBlockingQueue<Measurement> buffer = new LinkedBlockingQueue<Measurement>();

    public static void main(String[] args) throws MqttException {

        BrokerConnector subscriber = new BrokerSubscriber(BROKER_URI, TOPIC, QOS);

    }

}
