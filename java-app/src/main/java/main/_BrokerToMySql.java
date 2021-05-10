/*
package main;


import common.BrokerFetcher;
import common.SqlConnector;
import common.SqlPublisher;
import grp07.SqlDataHandler;
import grp07.Measurement;
import grp07.Sensor;
import grp07.Zone;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class BrokerToMySql {

    private static final String TOPIC = "t_sensores";// nome na especificação
    private static final int QOS = 0;

    private static final String MYSQL_URL_CLOUD = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";
    private static final String MYSQL_URL_LOCAL = "jdbc:mysql://localhost:3306/g07_local";

    private BrokerFetcher<Measurement> measurementFetcher;
    private SqlPublisher<Measurement> measurementPublisher;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public BrokerToMySql(String sourceUri, String targetUri, String[] collectionNames) throws SQLException {
        this.buffer = new ConcurrentHashMap<>();

        for (String collection : collectionNames) {
            try {
                this.measurementFetcher = new MeasurementFetcher(sourceUri, TOPIC, QOS);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            SqlConnector mysql_cloud = new SqlConnector(MYSQL_URL_CLOUD, "aluno", "aluno");
            SqlConnector mysql_local = new SqlConnector(targetUri, "root", "");
            SqlDataHandler mySqlDb = new SqlDataHandler(mysql_cloud, mysql_local);

            this.measurementPublisher = new MeasurementPublisher(MYSQL_URL_LOCAL, "root", "");
            this.measurementPublisher.start();
        }
    }

    protected ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> getBuffer() {
        return this.buffer;
    }

    public static void main(String[] args) throws SQLException {
        BrokerToMySql app = new BrokerToMySql(
                "tcp://broker.mqttdashboard.com:1883",
                "jdbc:mysql://localhost:3306/g07_local",
                new String[]{"sensort1", "sensort2"}
        );
    }


    private class MeasurementPublisher extends SqlPublisher<Measurement> {


        public MeasurementPublisher(String uri, String user, String pass) throws SQLException {
            super(uri, user, pass);
        }

        @Override
        protected PreparedStatement getStatement(Measurement measurement) throws SQLException {
            String id = measurement.getId().toString();
            Zone zone = mySqlDb.getZones().get(measurement.getZone());
            Sensor sensor = mySqlDb.getSensors().get(measurement.getSensor());
            String value = measurement.getValue();
            Timestamp date = new Timestamp(System.currentTimeMillis());


            //enviar para SQL
            String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = mysql_local.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, value);
            statement.setInt(3, zone.getId());
            statement.setInt(4, sensor.getId());
            statement.setTimestamp(5, date);
            statement.setBoolean(6, true); // TODO: deve receber um isValid
            return statement;
        }
    }

    private class MeasurementFetcher extends BrokerFetcher<Measurement> {


        @Override
        protected Class<Measurement> getMapperClass() {
            return Measurement.class;
        }

        public MeasurementFetcher(String uri, String topic, int qos) throws MqttException {
            super(uri, topic, qos);
        }
    }
}
 */