package grp02;

import common.*;
import grp07.Measurement;
import grp07.Sensor;
import grp07.SqlDataHandler;
import grp07.Zone;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class BrokerToMySql {
    private static final String TOPIC_PREFIX = "pisid_g07_";

    private BrokerFetcher<Measurement> measurementFetcher = null;
    private SqlPublisher<Measurement> measurementPublisher = null;
    private SqlDataHandler data;

    private ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> buffer;

    public BrokerToMySql(BrokerConnector brokerUri, SqlConnector sqlConn, String[] collectionNames) {
        this.buffer = new ConcurrentHashMap<>();
        this.data = new SqlDataHandler();

        for (String collection : collectionNames) {
            if (!buffer.containsKey(collection))
                buffer.put(collection, new LinkedBlockingQueue<>());

            try {
                measurementFetcher = new MeasurementFetcher(brokerUri, collection);
                measurementPublisher = new MeasurementPublisher(sqlConn, measurementFetcher.getBuffer());

            } catch (MqttException | SQLException e) {
                e.printStackTrace();
            }

            measurementPublisher.start();
        }
    }

    private SqlDataHandler getData() {
        return this.data;
    }

    public static void main(String[] args) {
        BrokerConnector brokerConn = null;
        SqlConnector sqlConn = null;

        String brokerUri = "tcp://broker.mqttdashboard.com:1883";
        String mysqlUri = "tcp://localhost:3306/g07";
        String mysqlUser = "root";
        String mysqlPass = "";
        String[] collectionNames = {"sensort1", "sensort2"};

        brokerConn = new BrokerConnector("tcp://broker.mqttdashboard.com:1883");
        try {
            sqlConn = new SqlConnector(mysqlUri, mysqlUser, mysqlPass);
            new BrokerToMySql(brokerConn, sqlConn, collectionNames);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private static class MeasurementFetcher extends BrokerFetcher<Measurement> {
        public MeasurementFetcher(BrokerConnector brokerConn, String collection) throws MqttException {
            super(brokerConn, BrokerToMySql.TOPIC_PREFIX + collection, 0);
        }

        @Override
        protected Class<Measurement> getMapperClass() {
            return Measurement.class;
        }
    }

    private class MeasurementPublisher extends SqlPublisher<Measurement> {

        public MeasurementPublisher(SqlConnector connector, LinkedBlockingQueue<Measurement> buffer) throws SQLException {
            super(connector, buffer);
        }

        @Override
        protected PreparedStatement getStatement(Measurement measurement) throws SQLException {
            String id = measurement.getId().toString();
            Zone zone = getData().getZones().get(measurement.getZone());
            Sensor sensor = getData().getSensors().get(measurement.getSensor());
            String value = measurement.getValue();
            Timestamp date = new Timestamp(System.currentTimeMillis());


            //enviar para SQL
            String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, value);
            statement.setInt(3, zone.getId());
            statement.setInt(4, sensor.getId());
            statement.setTimestamp(5, date);
            statement.setBoolean(6, true); // TODO: deve receber um isValid
            return statement;
        }
    }
}
