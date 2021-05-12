package grp02;

import common.BrokerPublisher;
import common.ClientToClient;
import grp07.ConnectToMongo;
import grp07.Measurement;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MongoToBroker implements ClientToClient<Measurement> {

    private static final String SOURCE_URI_ATLAS = "mongodb+srv://sid2021:sid2021@sid.yingw.mongodb.net/g07?retryWrites=true&w=majority";
    private static final String SOURCE_DB = "g07";

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "t_sensores";// nome na especificação
    private static final int QOS = 0;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public MongoToBroker(String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        for (String collection : collectionNames)
            this.buffer.put(collection, new LinkedBlockingQueue<>());
    }

    public static void main(String[] args) throws MqttException {

        String[] collectionNames = {"sensort1"};

        MongoToBroker mtb = new MongoToBroker(collectionNames);
        mtb.startFetching();
        mtb.startPublishing();

    }

    @Override
    public void startFetching() {
        new ConnectToMongo<Measurement>(SOURCE_URI_ATLAS, SOURCE_DB) {
            @Override
            protected void deal(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> collectionsDataBuffer) {

            }
        };
    }

    @Override
    public void startPublishing() {
        try {
            new BrokerPublisher<Measurement>(BROKER_URI, TOPIC, QOS).startPublishing(getBuffer());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return this.buffer;
    }
}
