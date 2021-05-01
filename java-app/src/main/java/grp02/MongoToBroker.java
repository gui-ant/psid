package grp02;

import grp07.ConnectToMongo;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MongoToBroker {

    private static final String SOURCE_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";

    public static void main(String[] args) {

        String[] collectionNames = {"sensort1"};

        try {

            ConnectToMongo cluster = new ConnectToMongo(SOURCE_URI_ATLAS, SOURCE_DB);

            ConnectToBroker broker = new ConnectToBroker(BROKER_URI);


            cluster.useCollections(collectionNames);
            cluster.startFetching();

            broker.startPublishing(cluster.getFetchingSource());

        } catch (MqttException e){
            e.printStackTrace();
        }
    }
}
