import com.google.gson.Gson;
import org.bson.Document;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class SqlSender {
    private Connection connection;
    private HashMap<String, Integer> sensorIDMap = new HashMap<>();
    private HashMap<String, Integer> zoneIDMap = new HashMap<>();


    //apenas estou a considerar o registo de measures, mais tarde faz-se o refactor
//    int id          = 4;
//    String data     = "100";
//    int sensor_id   = 1;
//    int zone_id     = 1;

    public SqlSender(Connection connection) {
        this.connection = connection;
        getSensorIDs();
        getZoneIDs();
    }


    private void getSensorIDs() {
        try {
            String sql = "SELECT * FROM sensors";
            Statement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery(sql);

            while (result.next()) {
                sensorIDMap.put(result.getString("name"), result.getInt("id"));
            }

            System.out.println(sensorIDMap);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void getZoneIDs() {
        try {
            String sql = "SELECT * FROM zones";
            Statement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery(sql);

            while (result.next()) {
                zoneIDMap.put(result.getString("name"), result.getInt("id"));
            }

            System.out.println(zoneIDMap);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public synchronized void send(Connection connection, Measurement measurement) {
        // buscar dados e extrair valores

        System.out.println("To insert: " + measurement);
        try {
//            String sql = "INSERT INTO measures (id, data, sensor_id, zone_id) VALUES (?, ?, ?, ?)";
//            PreparedStatement statement = connection.prepareStatement(sql);
//            statement.setInt(1, id);
//            statement.setString(2, data);
//            statement.setInt(3, sensor_id);
//            statement.setInt(4, zone_id);
//            int rows = statement.executeUpdate();
//            System.out.println("Inserted value successfully!!!");
//            statement.close();

            //obter atributos do objeto measurement
            String zone = measurement.getZona();
            String sensor = measurement.getSensor();
            String data = measurement.getMedicao();

            //obter id da zone e do sensor do mysql
            int zone_id = zoneIDMap.get(zone);
            int sensor_id = sensorIDMap.get(sensor);

            //enviar para SQL
            String sql = "INSERT INTO measures (data, sensor_id, zone_id) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, data);
            statement.setInt(2, sensor_id);
            statement.setInt(3, zone_id);

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

    /*
    public synchronized void send (Connection connection, Document sendable) {
        //enviar para SQL
        System.out.println(sendable.toJson());

    }
     */
}
