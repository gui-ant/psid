package grp02;

import common.BrokerSubscriber;
import common.MeasurementMySqlPublisher;
import common.MySqlPublisher;
import grp07.Measurement;
import grp07.MySqlData;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.sql.*;


public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "pisid_g07_sensors";// nome na especificação
    private static final int QOS = 0;

    private static final String MYSQL_URL_LOCAL = "jdbc:mysql://localhost:3306/g07_local";

    public static void main(String[] args) throws MqttException, SQLException {
        Connection mysql_local = DriverManager.getConnection(MYSQL_URL_LOCAL, "root", "");

        BrokerSubscriber<Measurement> subscriber = new BrokerSubscriber<>(BROKER_URI, TOPIC, QOS) {
            @Override
            protected Class<Measurement> getMapperClass() {
                return Measurement.class;
            }
        };

        MeasurementMySqlPublisher publisher = new MeasurementMySqlPublisher(mysql_local, subscriber.getBuffer()) {
            @Override
            protected void handle(Measurement measurement) {
                return;
            }

            @Override
            protected boolean isValid(Measurement m) {
                return true;
            }
        };

        publisher.start();
    }
}
