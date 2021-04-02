package grp07;

import java.sql.*;
import java.util.Hashtable;

public class SqlSender {
    private final Connection connCloud;
    //private final Connection connLocal;

    private final Hashtable<String, Sensor> sensors = new Hashtable<>();
    private final Hashtable<String, Zone> zones = new Hashtable<>();


    public SqlSender(Connection connCloud) {

        this.connCloud = connCloud;
        fetchZones();
        fetchSensors();
    }

    public Hashtable<String, Sensor> getSensors() {
        return sensors;
    }

    private void fetchZones() {
        String query = "SELECT * FROM zones";
        try (Statement st = connCloud.createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Zone z = new Zone(res.getInt("id"));
                zones.put(res.getString("name"), z);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void fetchSensors() {
        String query = "SELECT s.*, z.name FROM sensors as s JOIN zones as z on s.zone_id = z.id";
        try (Statement st = connCloud.createStatement()) {
            ResultSet res = st.executeQuery(query);

            while (res.next()) {
                Sensor s = new Sensor(res.getInt("id"));
                Zone z = zones.get(res.getString("z.name"));

                s.setMinLim(res.getInt("minlim"));
                s.setMaxLim(res.getInt("maxlim"));
                s.setZone(z);

                sensors.put(res.getString("name"), s);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public synchronized void send(Connection connection, Measurement measurement, boolean isValid) {

        // buscar dados e extrair valores

        System.out.println("To insert: " + measurement);
        try {
            String id = measurement.getId().toString();
            Zone zone = zones.get(measurement.getZone());
            Sensor sensor = sensors.get(measurement.getSensor());
            String value = measurement.getMeasure();
            //Timestamp date = measurement.getTimestamp();
            Timestamp date = new Timestamp(System.currentTimeMillis());


            //enviar para SQL
            String sql = "INSERT INTO measures (id, value, sensor_id, zone_id, date, isCorrect) VALUES (?, ?, ?, ?, ?, ?)";
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
