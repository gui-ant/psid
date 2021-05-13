package grp02;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.BrokerConnector;
import grp07.Measurement;
import grp07.MySqlData;
import grp07.MySqlData.*;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "pisid_g07_sensors";// nome na especificação
    private static final int QOS = 0;

    private static final String MYSQL_URL_LOCAL = "jdbc:mysql://194.210.86.10:3306/aluno_g07_local";

    public static void main(String[] args) throws MqttException, SQLException {

        BrokerSubscriber subscriber = new BrokerSubscriber(BROKER_URI, TOPIC, QOS);

        final Connection mysql_local = DriverManager.getConnection(MYSQL_URL_LOCAL, "root", "");
        MySqlData sender = new MySqlData();

        SQL_Publisher publisher = new SQL_Publisher(mysql_local, sender, subscriber.getBuffer());
        publisher.start();
    }

    public static class BrokerSubscriber extends BrokerConnector {

        LinkedBlockingQueue<Measurement> buffer;

        public BrokerSubscriber(String URI, String topic, int qos) throws MqttException {
            super(URI, topic, qos);
            buffer = new LinkedBlockingQueue<>();

            client.setCallback(insertInBufferCallback());
            tryConnect();
            client.subscribe(topic, this.qos);
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
                    System.out.println("Message arrived from broker (topic " + topic + "): " + message);

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

                    try {
                        Measurement m = objectMapper.readValue(message.toString(), Measurement.class);
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

    public static class SQL_Publisher extends Thread {

        private final Connection connection;
        private final MySqlData data;
        private final LinkedBlockingQueue<Measurement> buffer;

        public SQL_Publisher(Connection connection, MySqlData data, LinkedBlockingQueue<Measurement> buffer) {
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

            System.out.println("To insert: " + measurement);
            try {
                String id = measurement.getId().toString();
                Zone zone = this.data.getZones().get(measurement.getZone());
                Sensor sensor = this.data.getSensors().get(measurement.getSensor());
                String value = measurement.getValue();
                //Timestamp date = measurement.getTimestamp();
                Timestamp date = new Timestamp(System.currentTimeMillis());


                //enviar para SQL
                String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, isCorrect) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, id);
                statement.setString(2, value);
                statement.setInt(3, zone.getId());
                statement.setInt(4, sensor.getId());
                statement.setTimestamp(5, date);
                statement.setBoolean(6, isValid);


                int rows = statement.executeUpdate();
                if (rows > 0) {
                    System.out.println("Inserted value successfully!!!");
                }

                statement.close();


            } catch (Exception e) {
                System.out.println("Connection failed!!!");
                e.printStackTrace();
            }
        }
    }
}