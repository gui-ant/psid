package grp02;

import grp07.ConnectToMongo;
import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MongoToBroker {

    private static final String SOURCE_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "t_sensores";// nome na especificação
    private static final int QOS = 0;

    public static void main(String[] args) throws MqttException {

        String[] collectionNames = {"sensort1"};

        ConnectToMongo cluster = new ConnectToMongo(SOURCE_URI_ATLAS, SOURCE_DB);
        ConnectToBroker publisher = new ConnectToBroker(BROKER_URI);

        publisher.connectAsPublisher(TOPIC);

        cluster.useCollections(collectionNames);
        cluster.startFetching();

        publisher.startPublishing(cluster.getFetchingSource());

    }
}
