package grp02;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.BrokerConnector;
import common.IniConfig;
import grp07.Measurement;
import grp07.MySqlData;
import grp07.MySqlData.*;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionSQL extends IniConfig {

    public ConnectionSQL(String iniFile) {
        super(iniFile);

        String brokerUri = getConfig("broker", "uri");
        String brokerTopic = getConfig("broker", "topic");
        int brokerQos = Integer.parseInt(getConfig("broker", "qos"));
        String mysqlLocalUri = getConfig("mysql", "local_uri");

        try {
            BrokerSubscriber subscriber = new BrokerSubscriber(brokerUri, brokerTopic, brokerQos);
            try {
                Connection mysql_local = DriverManager.getConnection(mysqlLocalUri, "root", "");
                MySqlData data = new MySqlData("config.ini");

                SqlPublisher publisher = new SqlPublisher(mysql_local, data, subscriber.getBuffer());
                publisher.start();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MqttException, SQLException {
        new ConnectionSQL("config.ini");
    }

    public static class BrokerSubscriber extends BrokerConnector {

        LinkedBlockingQueue<Measurement> buffer;

        public BrokerSubscriber(String URI, String topic, int qos) throws MqttException {
            super(URI, topic, qos);
            buffer = new LinkedBlockingQueue<>();

            getClient().setCallback(insertInBufferCallback());
            tryConnect();
            getClient().subscribe(topic, getQos());
        }

        public LinkedBlockingQueue<Measurement> getBuffer() {
            return buffer;
        }

        private MqttCallback insertInBufferCallback() {
            return new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Connection lost!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

                    try {
                        Measurement m = objectMapper.readValue(message.toString(), Measurement.class);
                        System.out.println("Fetched (Broker):\t" + m);
                        buffer.offer(m);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            };
        }

    }

    public static class SqlPublisher extends Thread {

        private final Connection connection;
        private final MySqlData data;
        private final LinkedBlockingQueue<Measurement> buffer;

        public SqlPublisher(Connection connection, MySqlData data, LinkedBlockingQueue<Measurement> buffer) {
            this.connection = connection;
            this.data = data;
            this.buffer = buffer;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Measurement measurement = buffer.take();
                    send(connection, measurement, true);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        void send(Connection connection, Measurement measurement, boolean isValid) {

            // buscar dados e extrair valores
            try {
                String id = measurement.getId().toString();
                Zone zone = data.getZoneByName(measurement.getZone());
                Sensor sensor = data.getSensorByName(measurement.getSensor());
                String value = measurement.getValue();
                //Timestamp date = measurement.getTimestamp();
                Timestamp date = new Timestamp(System.currentTimeMillis());


                //enviar para SQL
                String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, id);
                statement.setString(2, value);
                statement.setInt(3, sensor.getId());
                statement.setInt(4, zone.getId());
                statement.setTimestamp(5, date);
                statement.setBoolean(6, isValid);


                int rows = statement.executeUpdate();
                if (rows > 0) {
                    System.out.println("Inserted (MySQL):\t" + measurement);
                }

                statement.close();


            } catch (Exception e) {
                System.out.println("Connection failed!!!");
                e.printStackTrace();
            }
        }
    }
}