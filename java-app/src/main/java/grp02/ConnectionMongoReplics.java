package grp02;

import common.BrokerConnector;
import common.MongoConnector;

/**
 * Através do programa ConnectionMongoReplics, os dados dos sensores são enviados para as réplicas Mongo
 * (réplicas inicializadas e localizadas em apenas 1 máquina) com periodicidade de 1 segundo.
 * Para a extração dos objetos das diversas (6) coleções que se encontram no MongoDB Nuvem,
 * irá ser utilizada apenas 1 thread – essa thread irá aceder a cada coleção a cada segundo e registar
 * nas respetivas coleções presentes nas réplicas.
 */

public class ConnectionMongoReplics {
    public static void main(String[] args) {
        String brokerUri = "tcp://broker.mqttdashboard.com:1883";
        MongoConnector mongo = new MongoConnector("mongodb://127.0.0.1:27017/g07");
        mongo.useDatabase("g07");
        String[] collectionNames = new String[]{"sensort1", "sensort2"};

        new BrokerToMongo(brokerUri, mongo, collectionNames);
    }
}
