package grp02;

import common.BrokerSubscriber;
import common.MySqlPublisher;
import grp07.Measurement;
import grp07.MySqlData;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionSQL {

    private static final String BROKER_URI = "tcp://broker.mqttdashboard.com:1883";
    private static final String TOPIC = "pisid_g07_sensors";// nome na especificação
    private static final int QOS = 0;

    private static final String MYSQL_URL_LOCAL = "jdbc:mysql://localhost:3306/g07_local";
    private static final String MYSQL_URL_CLOUD = "jdbc:mysql://194.210.86.10:3306/aluno_g07_cloud";

    public static void main(String[] args) throws MqttException, SQLException {

        BrokerSubscriber<Measurement> subscriber = new BrokerSubscriber<>(BROKER_URI, TOPIC, QOS) {
            @Override
            protected Class<Measurement> getMapperClass() {
                return Measurement.class;
            }
        };

        final Connection mysql_cloud = DriverManager.getConnection(MYSQL_URL_CLOUD, "aluno", "aluno");
        final Connection mysql_local = DriverManager.getConnection(MYSQL_URL_LOCAL, "root", "");

        MySqlPublisher<Measurement> publisher = new MySqlPublisher<>(mysql_local, MySqlData.get(), subscriber.getBuffer()) {
            @Override
            protected PreparedStatement getStatement(Measurement m) {
                return null;
            }
        };
        publisher.start();
    }

    class MeasurementPublisher extends MySqlPublisher<Measurement> {

        public MeasurementPublisher(Connection connection, MySqlData data, LinkedBlockingQueue<Measurement> buffer) {
            super(connection, data, buffer);
        }

        @Override
        protected PreparedStatement getStatement(Measurement measurement) {
            // buscar dados e extrair valores

            System.out.println("To insert: " + measurement);
            try {
                String id = measurement.getId().toString();
                //TODO - resolvi isto à pedreiro. Pode ser?
                MySqlData.Zone zone = getData().getZones().get(Long.parseLong(String.valueOf(measurement.getZone().charAt(1))));
                MySqlData.Sensor sensor = getData().getSensors().get(measurement.getSensor());
                String value = measurement.getValue();
                //Timestamp date = measurement.getTimestamp();
                Timestamp date = new Timestamp(System.currentTimeMillis());


                //enviar para SQL
                String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = getConnection().prepareStatement(sql);
                statement.setString(1, id);
                statement.setString(2, value);
                statement.setInt(3, zone.getId());
                statement.setInt(4, sensor.getId());
                statement.setTimestamp(5, date);
                statement.setBoolean(6, true); // TODO: Avaliar


                int rows = statement.executeUpdate();
                if (rows > 0) {
                    System.out.println("Inserted value successfully!!!");
                }

                statement.close();

                return statement;
            } catch (Exception e) {
                System.out.println("Connection failed!!!");
                e.printStackTrace();
            }
            return null;
        }
    }
}
