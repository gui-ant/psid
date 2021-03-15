import com.google.gson.Gson;
import org.bson.Document;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class SqlSender {
    private Connection connection;
    private HashMap<String, Integer> sensorIDMap = new HashMap<>();
    private HashMap<String, Integer> zoneIDMap = new HashMap<>();


    public SqlSender (Connection connection) {
        this.connection = connection;
        getSensorIDs();
        getZoneIDs();
    }

    private void getSensorIDs(){
        geTableRecordIDs("name" , "id", sensorIDMap);
    }

    private void getZoneIDs(){
        geTableRecordIDs("name" , "id", zoneIDMap);
    }

    private void geTableRecordIDs(String columnLabel1 , String columnLabel2 , HashMap<String, Integer> mapResult){
        try {
            String sql = "SELECT * FROM zones";
            Statement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery(sql);

            while (result.next()){
                mapResult.put(result.getString(columnLabel1), result.getInt(columnLabel2));
            }

            System.out.println(mapResult);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public synchronized void send (Connection connection, Measurement measurement) {
        // buscar dados e extrair valores

        System.out.println("To insert: " + measurement);
        try {

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
            if (rows > 0){
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
