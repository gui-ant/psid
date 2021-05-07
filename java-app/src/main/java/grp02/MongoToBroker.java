package grp02;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import common.CustomLogger;
import common.IniConfig;
import common.SysOutLogger;
import grp07.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.LoggerFactory;


public class MongoToBroker {
    private static final String INI_FILE = "config.ini";
    private final IniConfig config;
    private CustomLogger log;

    public MongoToBroker() {
        this.config = new IniConfig(INI_FILE);
        this.log = new SysOutLogger();
    }

    public void initialize() throws MqttException {
        config.useSection("params");
        String mongo_cluster_uri = config.get("mongo_cluster_uri");
        String mongo_cluster_db = config.get("mongo_cluster_db");
        String broker_uri = config.get("broker_uri");
        String broker_topic = config.get("broker_topic");
        String broker_qos = config.get("broker_qos");
        String collectionNames = config.get("collections");

        ConnectToMongo cluster = new ConnectToMongo(mongo_cluster_uri, mongo_cluster_db);
        BrokerPublisher publisher = new BrokerPublisher(broker_uri, broker_topic, Integer.parseInt(broker_qos));

        cluster.setLog(this.log);

        cluster.useCollections(collectionNames.split(","));
        cluster.startFetching();

        publisher.startPublishing(cluster.getFetchingSource());
    }

    public static void main(String[] args) {
        /* Linhas adicionas para desabilitar logs do mongo.driver na consola */
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
        try {
            new MongoToBroker().initialize();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
