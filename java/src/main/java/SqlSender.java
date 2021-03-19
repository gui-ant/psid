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

    public synchronized void send(Connection connection, MeasurementPOJO measurement) {

        // buscar dados e extrair valores

        System.out.println("To insert: " + measurement);
        try {
            Zone zone = zones.get(measurement.getZone());
            Sensor sensor = sensors.get(measurement.getSensor());
            String value = measurement.getMeasure();
            Timestamp date = measurement.getTimestamp();


            //enviar para SQL
            String sql = "INSERT INTO measures (value, sensor_id, zone_id,date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, value);
            statement.setInt(2, zone.getId());
            statement.setInt(3, sensor.getId());
            statement.setTimestamp(4, date);

            //verifica se a leitura está dentro dos limites do sensor
            if (Double.parseDouble(value) <= sensor.getMaxLim() && Double.parseDouble(value) >= sensor.getMaxLim()) {
                statement.setBoolean(5, true);
            } else {
                statement.setBoolean(5, false);
            }

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
