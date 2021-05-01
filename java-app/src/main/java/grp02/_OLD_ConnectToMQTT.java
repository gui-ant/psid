package grp02;
/*
import grp07.*;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.LinkedBlockingQueue;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

public class _OLD_ConnectToMQTT {

    final Mqtt5BlockingClient client;

    final static String HOST = "5ba500f83fc144c0864348d0d3c061a8.s1.eu.hivemq.cloud";
    final static String USERNAME = "grupo07";
    final static String PASSWORD = "Sid2021!";

    public _OLD_ConnectToMQTT(){

        client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(HOST)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();

        //estabelece a coneccao
        client.connectWith()
                .simpleAuth()
                .username(USERNAME)
                .password(UTF_8.encode(PASSWORD))
                .applySimpleAuth()
                .send();

        System.out.println("Connected successfully");

        //Confirmacao de msg recebida
        client.toAsync().publishes(ALL, publish -> {
            System.out.println("Received message: " + publish.getTopic() + " -> " + UTF_8.decode(publish.getPayload().get()));
        });

    }

    public _OLD_ConnectToMQTT(String host, String username, String password){

        client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();

        //estabelece a coneccao
        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();

        System.out.println("Connected successfully");

        //Confirmacao de msg recebida
        client.toAsync().publishes(ALL, publish -> {
            System.out.println("Received message: " + publish.getTopic() + " -> " + UTF_8.decode(publish.getPayload().get()));
        });

    }

    public void subscibe(String topic){
        //subscreve o topico
        client.subscribeWith()
                .topicFilter(topic)
                .send();
    }

    public void publish(String topic, String msg){
        //publica a msg no topico
        client.publishWith()
                .topic(topic)
                .payload(UTF_8.encode("msg"))
                .send();
    }

}
 */